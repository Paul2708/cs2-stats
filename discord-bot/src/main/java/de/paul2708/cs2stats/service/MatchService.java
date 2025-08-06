package de.paul2708.cs2stats.service;

import de.paul2708.cs2stats.entity.SteamUser;
import de.paul2708.cs2stats.repository.MatchRepository;
import de.paul2708.cs2stats.repository.SteamUserRepository;
import de.paul2708.cs2stats.steam.DemoProviderClient;
import de.paul2708.cs2stats.steam.Match;
import de.paul2708.cs2stats.steam.SteamClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Duration;
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
        this.executorService.submit(() -> requestMatches(steamUser));
    }

    public void fetchLatestMatchesPeriodically() {
        periodicExecutorService.submit(() -> {
            while (true) {
                logger.info("Start fetching latest matches for all registered users.");
                try {
                    for (SteamUser steamUser : steamUserRepository.findAll()) {
                        fetchLatestMatches(steamUser);
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

    private void requestMatches(SteamUser steamUser) {
        logger.info("Start to request matches for Steam user {}", steamUser.steamId());

        try {
            steamClient.requestConsecutiveCodes(steamUser.steamId(), steamUser.authenticationCode(), steamUser.lastKnownShareCode().shareCode(),
                    shareCode -> {
                        if (matchRepository.findMatchById(shareCode.matchId()).isPresent()) {
                            logger.info("Found new match but skipped because it is already stored");
                            return;
                        }

                        try {
                            Match match = demoProviderClient.requestMatch(shareCode);
                            matchRepository.create(match);

                            logger.info("Found and stored new match with ID {}", shareCode.matchId());
                        } catch (Exception e) {
                            logger.error("Failed to request match demo for share code {}", shareCode, e);
                        }
                    });
        } catch (IOException | InterruptedException e) {
            logger.error("Failed to fetch consecutive share codes", e);
        }

        logger.info("Completed.");
    }
}
