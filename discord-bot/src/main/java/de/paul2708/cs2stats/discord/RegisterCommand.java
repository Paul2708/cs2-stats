package de.paul2708.cs2stats.discord;

import de.paul2708.cs2stats.entity.SteamUser;
import de.paul2708.cs2stats.repository.SteamUserRepository;
import de.paul2708.cs2stats.service.MatchService;
import de.paul2708.cs2stats.steam.ShareCode;
import de.paul2708.cs2stats.steam.SteamClient;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Optional;

public class RegisterCommand extends ListenerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(RegisterCommand.class);

    private final SteamUserRepository steamUserRepository;
    private final MatchService matchService;

    private final SteamClient steamClient;

    public RegisterCommand(SteamUserRepository steamUserRepository, MatchService matchService, SteamClient steamClient) {
        this.steamUserRepository = steamUserRepository;
        this.matchService = matchService;
        this.steamClient = steamClient;
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

                String discordUsername = event.getUser().getName();

                // Validate arguments
                String resolvedSteamId = null;
                try {
                    if (steamId.matches("[0-9]+")) {
                        if (!steamClient.requestExistence(steamId)) {
                            event.reply("The provided Steam ID does not exist. Please use a third-party website like https://steamidcheck.com to get your Steam ID.")
                                    .setEphemeral(true)
                                    .queue();
                            return;
                        }

                        resolvedSteamId = steamId;
                    }

                    if (resolvedSteamId == null) {
                        Optional<String> steamIdOptional = steamClient.requestSteamId(steamId);
                        if (steamIdOptional.isEmpty()) {
                            event.reply("The provided Steam ID does not exist. Please use a third-party website like https://steamidcheck.com to get your Steam ID.")
                                    .setEphemeral(true)
                                    .queue();
                            return;
                        }

                        resolvedSteamId = steamIdOptional.get();
                    }
                } catch (IOException | InterruptedException e) {
                    logger.error("Failed to validate Steam ID", e);

                    event.reply("Failed to validate your Steam ID. Please report this issue to the developers.")
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
                if (steamUserRepository.findByDiscordName(discordUsername).isPresent()) {
                    event.reply("You already registered a Steam account.")
                            .setEphemeral(true)
                            .queue();
                    return;
                }

                // Register user
                SteamUser steamUser = new SteamUser(resolvedSteamId, parsedShareCode, authenticationCode, parsedShareCode,
                        discordUsername);
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
