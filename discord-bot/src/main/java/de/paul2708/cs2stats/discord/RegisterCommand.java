package de.paul2708.cs2stats.discord;

import de.paul2708.cs2stats.entity.SteamUser;
import de.paul2708.cs2stats.repository.SteamUserRepository;
import de.paul2708.cs2stats.service.MatchService;
import de.paul2708.cs2stats.steam.ShareCode;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RegisterCommand extends ListenerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(RegisterCommand.class);

    private final SteamUserRepository steamUserRepository;
    private final MatchService matchService;

    public RegisterCommand(SteamUserRepository steamUserRepository, MatchService matchService) {
        this.steamUserRepository = steamUserRepository;
        this.matchService = matchService;
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (event.getName().equals("register")) {
            logger.info("Handle /register command issued by {}", event.getUser().getName());

            OptionMapping steamIdOption = event.getOption("steamid");
            OptionMapping shareCodeOption = event.getOption("sharecode");
            OptionMapping authenticationCodeOption = event.getOption("authenticationcode");

            if (steamIdOption != null && shareCodeOption != null && authenticationCodeOption != null) {
                String steamId = steamIdOption.getAsString();
                String shareCode = shareCodeOption.getAsString();
                String authenticationCode = authenticationCodeOption.getAsString();

                // Validate arguments
                if (!steamId.matches("[0-9]+")) {
                    event.reply("Illegal Steam ID. The Steam ID contains only digits. Please use a third-party website like https://steamidcheck.com to get your Steam ID.")
                            .setEphemeral(true)
                            .queue();
                    return;
                }

                ShareCode parsedShareCode;
                try {
                    parsedShareCode = ShareCode.fromCode(shareCode);
                } catch (IllegalArgumentException e) {
                    event.reply("Illegal share code. Did you copy the right one, starting with CSGO-?")
                            .setEphemeral(true)
                            .queue();
                    return;
                }

                if (steamUserRepository.findBySteamId(steamId).isPresent()) {
                    event.reply("The Steam user with Steam ID %s is already registered.".formatted(steamId))
                            .setEphemeral(true)
                            .queue();
                    return;
                }

                // Register user
                SteamUser steamUser = new SteamUser(steamId, parsedShareCode, authenticationCode, ShareCode.NONE);
                steamUserRepository.create(steamUser);

                matchService.fetchLatestMatches(steamUser);

                String message = """
                        ✅ **Registered successfully!**
                        
                        ⏳ We are now **fetching your previous CS2 matches**.
                        This process may take a **minute or two**, so please be patient.
                        It's a background task so you won't be notified.
                        """;

                event.reply(message)
                        .setEphemeral(true)
                        .queue();

                logger.info("Discord user {} registered their steam account with Steam ID {}",
                        event.getUser().getName(), steamUser.steamId());
            }
        }
    }
}
