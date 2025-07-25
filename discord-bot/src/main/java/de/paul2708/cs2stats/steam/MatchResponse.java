package de.paul2708.cs2stats.steam;

public record MatchResponse(
        String matchId,
        String demoUrl,
        Match match,
        long matchTime
) {
}
