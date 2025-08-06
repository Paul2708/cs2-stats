package de.paul2708.cs2stats.discord;

import de.paul2708.cs2stats.chart.RankHistory;
import de.paul2708.cs2stats.chart.RankHistoryChart;
import de.paul2708.cs2stats.entity.SteamUser;
import de.paul2708.cs2stats.repository.MatchRepository;
import de.paul2708.cs2stats.repository.SteamUserRepository;
import de.paul2708.cs2stats.steam.*;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.utils.FileUpload;

import java.util.*;

public class RankHistoryCommand extends ListenerAdapter {

    private final SteamUserRepository steamUserRepository;
    private final MatchRepository matchRepository;

    public RankHistoryCommand(SteamUserRepository steamUserRepository, MatchRepository matchRepository) {
        this.steamUserRepository = steamUserRepository;
        this.matchRepository = matchRepository;
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (event.getName().equals("ranks")) {
            List<Match> matches = matchRepository.findAll();
            List<String> steamIds = steamUserRepository.findAll().stream()
                    .map(SteamUser::steamId)
                    .toList();

            List<RankHistory> rankHistories = new ArrayList<>();
            Map<String, Map<Date, Integer>> ranks = new HashMap<>();
            Map<String, String> displayNames = new HashMap<>();

            for (String steamId : steamIds) {
                ranks.put(steamId, new HashMap<>());
            }

            for (Match match : matches) {
                for (PlayerStats playerStats : match.matchDetails().scoreboard()) {
                    if (steamIds.contains(playerStats.steamId())) {
                        ranks.get(playerStats.steamId()).put(new Date(match.matchTime()), playerStats.updatedRank());
                        displayNames.put(playerStats.steamId(), playerStats.name());
                    }
                }
            }

            for (String steamId : ranks.keySet()) {
                rankHistories.add(new RankHistory(displayNames.get(steamId), ranks.get(steamId)));
            }

            byte[] plot = RankHistoryChart.plot(rankHistories);
            event.reply("Rank history")
                    .addFiles(FileUpload.fromData(plot, "plot.png"))
                    .setEphemeral(true)
                    .queue();
        }
    }
}
