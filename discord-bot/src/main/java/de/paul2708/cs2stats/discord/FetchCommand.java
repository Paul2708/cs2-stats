package de.paul2708.cs2stats.discord;

import de.paul2708.cs2stats.service.MatchService;
import de.paul2708.cs2stats.steam.ShareCode;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FetchCommand extends ListenerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(FetchCommand.class);

    private final MatchService matchService;

    public FetchCommand(MatchService matchService) {
        this.matchService = matchService;
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (event.getName().equals("fetch")) {
            logger.info("Handle /fetch command issued by {}", event.getUser().getName());

            OptionMapping shareCodeOption = event.getOption("sharecode");

            if (shareCodeOption != null) {
                String shareCode = shareCodeOption.getAsString();

                // Validate argument
                ShareCode parsedShareCode;
                try {
                    parsedShareCode = ShareCode.fromCode(shareCode);
                } catch (IllegalArgumentException e) {
                    event.reply("Illegal share code. Did you copy the right one, starting with CSGO-?")
                            .setEphemeral(true)
                            .queue();
                    return;
                }

                String message = """
                        ⏳ Fetch is in progress.
                        
                        Please check `/ranks` to see if a new match was added.
                        """;

                event.reply(message)
                        .setEphemeral(true)
                        .queue();

                matchService.fetchSingleMatch(parsedShareCode);
            }
        }
    }
}
