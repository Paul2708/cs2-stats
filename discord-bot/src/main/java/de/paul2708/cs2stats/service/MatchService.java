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

import java.io.IOException;
import java.time.Duration;
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
        this.executorService.submit(() -> requestMatches(steamUser, false));
    }

    public void fetchLatestMatchesPeriodically() {
        periodicExecutorService.submit(() -> {
            //noinspection InfiniteLoopStatement
            while (true) {
                logger.info("Start fetching latest matches for all registered users.");
                try {
                    for (SteamUser steamUser : steamUserRepository.findAll()) {
                        this.executorService.submit(() -> requestMatches(steamUser, true));
                        Thread.sleep(Duration.ofMinutes(1));
                    }

                    Thread.sleep(Duration.ofMinutes(5));
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                logger.info("Fetch completed.");
            }
        });
    }

    public void fetchSingleMatch(ShareCode shareCode) {
        logger.info("Start fetching single match manually with share code {} (match ID:{})", shareCode.shareCode(),
                shareCode.matchId());

        this.executorService.submit(() -> this.requestSingleMatch(shareCode));
    }

    private void requestSingleMatch(ShareCode shareCode) {
        if (matchRepository.findMatchById(shareCode.matchId()).isPresent()) {
            logger.info("Found new match but skipped because it is already stored");
            return;
        }

        try {
            Optional<Match> matchOpt = demoProviderClient.requestMatch(shareCode);

            if (matchOpt.isPresent()) {
                matchRepository.create(matchOpt.get());
                logger.info("Found and stored new match with ID {}", shareCode.matchId());
            } else {
                logger.warn("Found match, but the demo with share code {} is no longer accessible",
                        shareCode.shareCode());
            }
        } catch (Exception e) {
            logger.error("Failed to request match demo for share code {}", shareCode, e);
        }
    }

    private void requestMatches(SteamUser steamUser, boolean skipIfEqualCodes) {
        logger.info("Start to request matches for Steam user {}", steamUser.steamId());

        try {
            steamClient.requestConsecutiveCodes(steamUser.steamId(), steamUser.authenticationCode(), steamUser.lastKnownShareCode().shareCode(), shareCode -> {
                if (skipIfEqualCodes && steamUser.lastKnownShareCode().equals(shareCode)) {
                    logger.info("No new match found.");
                    return;
                }

                steamUserRepository.updateLastKnownShareCode(steamUser.steamId(), shareCode.shareCode());

                requestSingleMatch(shareCode);
            });
        } catch (IOException | InterruptedException e) {
            logger.error("Failed to fetch consecutive share codes", e);
        }

        logger.info("Completed.");
    }
}
