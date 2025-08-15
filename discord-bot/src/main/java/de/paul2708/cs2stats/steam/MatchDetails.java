package de.paul2708.cs2stats.steam;

import java.util.List;

public record MatchDetails(
        String map,
        List<PlayerStats> scoreboard
) {

    public PlayerStats getPlayerStats(String steamId) {
        return scoreboard.stream()
                .filter(playerStats -> playerStats.steamId().equals(steamId))
                .findFirst()
                .orElseThrow();
    }
}
