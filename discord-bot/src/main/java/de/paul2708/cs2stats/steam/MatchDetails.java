package de.paul2708.cs2stats.steam;

import java.util.List;

public record MatchDetails(
        String map,
        List<PlayerStats> scoreboard
) {}
