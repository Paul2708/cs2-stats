package de.paul2708.cs2stats.discord.history;

import de.paul2708.cs2stats.steam.Match;

public record AnnotatedMatch(Match match, int position, String eloDiff, String playerSteamId)
{
}
