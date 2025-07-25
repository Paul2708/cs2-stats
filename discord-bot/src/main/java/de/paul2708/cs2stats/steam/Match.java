package de.paul2708.cs2stats.steam;

import java.util.List;

public record Match(
        String map,
        List<PlayerStats> scoreboard
) {}
