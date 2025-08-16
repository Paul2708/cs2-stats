package de.paul2708.cs2stats.steam;

public record PlayerStats(
        String name,
        String steamId,
        int deaths,
        int kills,
        int assists,
        int headshotKills,
        int aces,
        int fourKRounds,
        int threeKRounds,
        int damage,
        int utilityDamage,
        int enemiesFlashed,
        int cashEarned,
        int mvps,
        int updatedRank,
        int totalRounds,
        int teamRounds
) {
}
