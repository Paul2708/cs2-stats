package de.paul2708.cs2stats.discord.history;

import com.github.freva.asciitable.AsciiTable;
import com.github.freva.asciitable.Column;
import de.paul2708.cs2stats.util.Pagination;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class MatchListPagination extends Pagination<AnnotatedMatch, MatchListPagination.Page> {

    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("dd. MMMM yyyy HH:mm:ss");
    private static final int PAGE_SIZE = 5;

    private final String steamId;

    public MatchListPagination(String steamId, List<AnnotatedMatch> items) {
        super(items, PAGE_SIZE);

        this.steamId = steamId;
    }

    @Override
    public Page renderPage(int page) {
        Button backButton = Button.primary("backBtn:%d".formatted(page), "Previous")
                .withEmoji(Emoji.fromUnicode("◀"))
                .withDisabled(!hasPreviousPage(page));
        Button nextButton = Button.primary("nextBtn:%d".formatted(page), "Next")
                .withEmoji(Emoji.fromUnicode("▶"))
                .withDisabled(!hasNextPage(page));

        String table = generateTable(steamId, sliceItems(page));
        return new Page(backButton, nextButton, "```\n%s\n```".formatted(table));
    }

    private String generateTable(String steamId, List<AnnotatedMatch> matches) {
        return AsciiTable.getTable(AsciiTable.FANCY_ASCII, matches, Arrays.asList(
                        new Column()
                                .with(match -> String.valueOf(match.position())),
                        new Column()
                                .header("Date")
                                .with(match -> DATE_FORMAT.format(new Date(match.match().matchTime() * 1000L))),
                        new Column()
                                .header("Map")
                                .with(match -> match.match().matchDetails().map()),
                        new Column()
                                .header("Kills")
                                .with(match -> String.valueOf(match.match().matchDetails().getPlayerStats(steamId).kills())),
                        new Column()
                                .header("Deaths")
                                .with(match -> String.valueOf(match.match().matchDetails().getPlayerStats(steamId).deaths())),
                        new Column()
                                .header("MVPs")
                                .with(match -> String.valueOf(match.match().matchDetails().getPlayerStats(steamId).mvps())),
                        new Column()
                                .header("Rank")
                                .with(match -> String.valueOf(match.match().matchDetails().getPlayerStats(steamId).updatedRank())),
                        new Column()
                                .header("Elo Gain/Loss")
                                .with(AnnotatedMatch::eloDiff)
                )
        );
    }

    public record Page(Button backButton, Button nextButton, String content) {

    }
}
