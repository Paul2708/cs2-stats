package de.paul2708.cs2stats.service;

import de.paul2708.cs2stats.entity.SteamUser;
import de.paul2708.cs2stats.repository.MatchRepository;
import de.paul2708.cs2stats.repository.SteamUserRepository;
import de.paul2708.cs2stats.steam.DemoProviderClient;
import de.paul2708.cs2stats.steam.Match;
import de.paul2708.cs2stats.steam.SteamClient;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MatchService {

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

    private void requestMatches(SteamUser steamUser) {
        try {
            steamClient.requestConsecutiveCodes(steamUser.steamId(), steamUser.authenticationCode(), steamUser.lastKnownShareCode().shareCode(),
                    shareCode -> {
                        if (matchRepository.findMatchById(shareCode.matchId()).isPresent()) {
                            System.out.println("Skipped match because already stored");
                            return;
                        }

                        try {
                            Match match = demoProviderClient.requestMatch(shareCode);
                            System.out.println(match);

                            matchRepository.create(match);
                        } catch (Exception e) {
                            System.out.println("Failed to fetch previous matches");
                            e.printStackTrace();
                        }
                    });
        } catch (IOException | InterruptedException e) {
            System.out.println("Failed to fetch previous matches");
            e.printStackTrace();
        }
    }
}
