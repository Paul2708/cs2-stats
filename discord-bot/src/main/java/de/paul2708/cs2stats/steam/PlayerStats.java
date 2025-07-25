package de.paul2708.cs2stats.steam;

public record PlayerStats(
        String name,
        String steamId,
        int deaths,
        int kills,
        int mvps,
        int updatedRank
) {
}
