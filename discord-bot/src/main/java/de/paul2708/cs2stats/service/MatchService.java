package de.paul2708.cs2stats.service;

import de.paul2708.cs2stats.entity.SteamUser;
import de.paul2708.cs2stats.repository.MatchRepository;
import de.paul2708.cs2stats.repository.SteamUserRepository;
import de.paul2708.cs2stats.steam.DemoProviderClient;
import de.paul2708.cs2stats.steam.Match;
import de.paul2708.cs2stats.steam.ShareCode;
import de.paul2708.cs2stats.steam.SteamClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MatchService {

    private static final Logger logger = LoggerFactory.getLogger(MatchService.class);

    private final SteamUserRepository steamUserRepository;
    private final MatchRepository matchRepository;

    private final SteamClient steamClient;
    private final DemoProviderClient demoProviderClient;

    private final ExecutorService executorService;
    private final ExecutorService periodicExecutorService;

    public MatchService(SteamUserRepository steamUserRepository, MatchRepository matchRepository, SteamClient steamClient, DemoProviderClient demoProviderClient) {
        this.steamUserRepository = steamUserRepository;
        this.matchRepository = matchRepository;
        this.steamClient = steamClient;
        this.demoProviderClient = demoProviderClient;

        this.executorService = Executors.newSingleThreadExecutor();
        this.periodicExecutorService = Executors.newSingleThreadExecutor();
    }

    public void fetchLatestMatches(SteamUser steamUser) {
        this.executorService.submit(() -> requestMatches(steamUser.steamId()));
    }

    public void fetchLatestMatchesPeriodically() {
        periodicExecutorService.submit(() -> {
            //noinspection InfiniteLoopStatement
            while (true) {
                try {
                    for (SteamUser steamUser : steamUserRepository.findAll()) {
                        fetchLatestMatches(steamUser);
                        Thread.sleep(Duration.ofMinutes(1));
                    }

                    Thread.sleep(Duration.ofMinutes(5));
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    public void fetchSingleMatch(ShareCode shareCode) {
        logger.info("Start fetching single match manually with share code {} (match ID:{})", shareCode.shareCode(),
                shareCode.matchId());

        if (matchRepository.findMatchById(shareCode.matchId()).isPresent()) {
            logger.info("Found a match but skipped because it is already stored");
            return;
        }

        this.executorService.submit(() -> this.requestSingleMatch(shareCode));
    }

    private void requestSingleMatch(ShareCode shareCode) {
        try {
            Optional<Match> matchOpt = demoProviderClient.requestMatch(shareCode);

            if (matchOpt.isPresent()) {
                Match match = matchOpt.get();

                if (!match.isPremier()) {
                    logger.warn("Found match, but it's not a Premier match.");
                    return;
                }

                matchRepository.create(match);
                logger.info("Found and stored new match with ID {}", shareCode.matchId());
            } else {
                logger.warn("Found match, but the demo with share code {} is no longer accessible",
                        shareCode.shareCode());
            }
        } catch (Exception e) {
            logger.error("Failed to request match demo for share code {}", shareCode, e);
        }
    }

    private void requestMatches(String steamUserId) {
        SteamUser steamUser = steamUserRepository.findBySteamId(steamUserId)
                .orElseThrow();

        logger.info("Start to request matches for user {} (Steam ID {})", steamUser.discordUserName(), steamUser.steamId());

        boolean registration = steamUser.initialShareCode().equals(steamUser.lastKnownShareCode());

        try {
            List<ShareCode> shareCodes = steamClient.requestConsecutiveCodes(steamUser.steamId(), steamUser.authenticationCode(), steamUser.lastKnownShareCode().shareCode());

            if (!registration) {
                shareCodes.removeFirst();
            } else {
                logger.warn("Include initial share code due to a new registration");
            }

            if (shareCodes.isEmpty()) {
                logger.info("Steam didn't found a new share code.");
                return;
            }

            logger.info("Steam found {} new share code(s)", shareCodes.size());

            for (ShareCode shareCode : shareCodes) {
                logger.info("Requesting match for share code {}...", shareCode.shareCode());

                if (matchRepository.findMatchById(shareCode.matchId()).isPresent()) {
                    steamUserRepository.updateLastKnownShareCode(steamUser.steamId(), shareCode.shareCode());

                    logger.info("Found a match but skipped because it is already stored");
                    continue;
                }

                if (!registration) {
                    logger.info("We wait two minutes to ensure that the demo is *really* ready...");
                    try {
                        Thread.sleep(Duration.ofMinutes(2));
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }

                logger.info("Proceeding to request game from demo provider");

                requestSingleMatch(shareCode);

                steamUserRepository.updateLastKnownShareCode(steamUser.steamId(), shareCode.shareCode());
            }
        } catch (Exception e) {
            logger.error("Failed to fetch consecutive share codes", e);
        }

        logger.info("Fetch completed.");
    }
}
