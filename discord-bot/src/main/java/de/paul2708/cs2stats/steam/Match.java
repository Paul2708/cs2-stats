package de.paul2708.cs2stats.steam;

public record Match(
        String matchId,
        String demoUrl,
        MatchDetails matchDetails,
        long matchTime
) {
}
