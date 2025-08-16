package de.paul2708.cs2stats.discord.history;

import de.paul2708.cs2stats.repository.MatchRepository;
import de.paul2708.cs2stats.repository.SteamUserRepository;
import de.paul2708.cs2stats.steam.Match;
import de.paul2708.cs2stats.steam.PlayerStats;
import de.paul2708.cs2stats.util.Pagination;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class HistoryCommand extends ListenerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(HistoryCommand.class);

    private final SteamUserRepository steamUserRepository;
    private final MatchRepository matchRepository;

    private final Map<String, MatchListPagination> paginations;

    public HistoryCommand(SteamUserRepository steamUserRepository, MatchRepository matchRepository) {
        this.steamUserRepository = steamUserRepository;
        this.matchRepository = matchRepository;

        this.paginations = new HashMap<>();
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

                // Filter player matches
                List<Match> matches = matchRepository.findAll()
                        .stream()
                        .filter(match -> match.matchDetails().scoreboard().stream()
                                .map(PlayerStats::steamId)
                                .collect(Collectors.toSet()).
                                contains(steamId))
                        .sorted(Comparator.comparingLong(Match::matchTime))
                        .toList()
                        .reversed();

                // Compute elo diff
                Map<Match, OptionalInt> eloDiff = new HashMap<>();

                for (int i = 0; i < matches.size() - 1; i++) {
                    int rank = matches.get(i)
                            .matchDetails()
                            .getPlayerStats(steamId)
                            .updatedRank();
                    int previousRank = matches.get(i + 1)
                            .matchDetails()
                            .getPlayerStats(steamId)
                            .updatedRank();

                    if (rank == 0 || previousRank == 0) {
                        eloDiff.put(matches.get(i), OptionalInt.empty());
                    } else {
                        eloDiff.put(matches.get(i), OptionalInt.of(rank - previousRank));
                    }
                }

                if (!matches.isEmpty()) {
                    eloDiff.put(matches.getLast(), OptionalInt.empty());
                }

                // Construct annotated matches
                List<AnnotatedMatch> annotatedMatches = new ArrayList<>();
                for (int i = 0; i < matches.size(); i++) {
                    Match match = matches.get(i);

                    annotatedMatches.add(new AnnotatedMatch(match, i + 1, formatEloDiff(eloDiff.get(match)), steamId));
                }

                paginations.put(event.getUser().getId(), new MatchListPagination(steamId, annotatedMatches));

                // Send message
                MatchListPagination pagination = paginations.get(event.getUser().getId());
                MatchListPagination.Page page = pagination.renderPage(Pagination.FIRST_PAGE);

                event.reply(page.content())
                        .addActionRow(page.backButton(), page.nextButton())
                        .setEphemeral(true)
                        .queue();
            }
        }
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        if (event.getComponentId().startsWith("nextBtn")) {
            MatchListPagination pagination = paginations.get(event.getUser().getId());

            int currentPage = Integer.parseInt(event.getComponentId().split(":")[1]);
            MatchListPagination.Page page = pagination.renderPage(currentPage + 1);

            event.editMessage(page.content())
                    .setActionRow(page.backButton(), page.nextButton())
                    .queue();
            return;
        }
        if (event.getComponentId().startsWith("backBtn")) {
            MatchListPagination pagination = paginations.get(event.getUser().getId());

            int currentPage = Integer.parseInt(event.getComponentId().split(":")[1]);
            MatchListPagination.Page page = pagination.renderPage(currentPage - 1);

            event.editMessage(page.content())
                    .setActionRow(page.backButton(), page.nextButton())
                    .queue();
            return;
        }
    }

    private String formatEloDiff(OptionalInt eloDiffOpt) {
        if (eloDiffOpt.isEmpty()) {
            return "-/-";
        }

        int eloDiff = eloDiffOpt.getAsInt();
        if (eloDiff > 0) {
            return "+%d".formatted(eloDiff);
        }

        return String.valueOf(eloDiff);
    }
}
