package de.paul2708.cs2stats.discord;

import com.github.freva.asciitable.AsciiTable;
import com.github.freva.asciitable.Column;
import de.paul2708.cs2stats.repository.MatchRepository;
import de.paul2708.cs2stats.repository.SteamUserRepository;
import de.paul2708.cs2stats.steam.Match;
import de.paul2708.cs2stats.steam.PlayerStats;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class HistoryCommand extends ListenerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(HistoryCommand.class);

    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("dd. MMMM yyyy HH:mm:ss");

    private final SteamUserRepository steamUserRepository;
    private final MatchRepository matchRepository;

    public HistoryCommand(SteamUserRepository steamUserRepository, MatchRepository matchRepository) {
        this.steamUserRepository = steamUserRepository;
        this.matchRepository = matchRepository;
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (event.getName().equals("history")) {
            logger.info("Handle /history command issued by {}", event.getUser().getName());

            OptionMapping steamIdOption = event.getOption("steamid");

            if (steamIdOption != null) {
                String steamId = steamIdOption.getAsString();

                if (steamUserRepository.findBySteamId(steamId).isEmpty()) {
                    event.reply("The Steam user with Steam ID %s is not registered.".formatted(steamId))
                            .setEphemeral(true)
                            .queue();
                    return;
                }

                List<Match> matches = matchRepository.findAll()
                        .stream()
                        .filter(match -> match.matchDetails().scoreboard().stream()
                                .map(PlayerStats::steamId)
                                .collect(Collectors.toSet()).
                                contains(steamId))
                        .sorted(Comparator.comparingLong(Match::matchTime))
                        .limit(5)
                        .toList()
                        .reversed();

                String table = AsciiTable.getTable(AsciiTable.FANCY_ASCII, matches, Arrays.asList(
                                new Column()
                                        .header("Date")
                                        .with(match -> DATE_FORMAT.format(new Date(match.matchTime() * 1000L))),
                                new Column()
                                        .header("Map")
                                        .with(match -> match.matchDetails().map()),
                                new Column()
                                        .header("Kills")
                                        .with(match -> String.valueOf(match.matchDetails().scoreboard().stream()
                                                .filter(playerStats -> playerStats.steamId().equals(steamId))
                                                .findFirst()
                                                .get()
                                                .kills())),
                                new Column()
                                        .header("Deaths")
                                        .with(match -> String.valueOf(match.matchDetails().scoreboard().stream()
                                                .filter(playerStats -> playerStats.steamId().equals(steamId))
                                                .findFirst().
                                                get()
                                                .deaths())),

                                new Column()
                                        .header("MVPs")
                                        .with(match -> String.valueOf(match.matchDetails().scoreboard().stream()
                                                .filter(playerStats -> playerStats.steamId().equals(steamId))
                                                .findFirst()
                                                .get()
                                                .mvps())),

                                new Column()
                                        .header("New Rank")
                                        .with(match -> String.valueOf(match.matchDetails().scoreboard().stream()
                                                .filter(playerStats -> playerStats.steamId().equals(steamId))
                                                .findFirst().
                                                get()
                                                .updatedRank()))

                        )
                );

                event.reply("**Last " + matches.size() + " Matches**\n```\n" + table + "\n```")
                        .setEphemeral(true)
                        .queue();
            }
        }
    }
}
