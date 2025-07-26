package de.paul2708.cs2stats.discord;

import de.paul2708.cs2stats.entity.SteamUser;
import de.paul2708.cs2stats.repository.SteamUserRepository;
import de.paul2708.cs2stats.steam.ShareCode;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

public class RegisterCommand extends ListenerAdapter {

    private final SteamUserRepository steamUserRepository;

    public RegisterCommand(SteamUserRepository steamUserRepository) {
        this.steamUserRepository = steamUserRepository;
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (event.getName().equals("register")) {
            OptionMapping steamIdOption = event.getOption("steamid");
            OptionMapping shareCodeOption = event.getOption("sharecode");
            OptionMapping authenticationCodeOption = event.getOption("authenticationcode");

            if (steamIdOption != null && shareCodeOption != null && authenticationCodeOption != null) {
                String steamId = steamIdOption.getAsString();
                String shareCode = shareCodeOption.getAsString();
                String authenticationCode = authenticationCodeOption.getAsString();

                // Register
                ShareCode parsedShareCode;
                try {
                    parsedShareCode = ShareCode.fromCode(shareCode);
                } catch (IllegalArgumentException e) {
                    event.reply("Illegal share code. Did you copy the right one, starting with CSGO-?")
                            .setEphemeral(true)
                            .queue();
                    return;
                }

                SteamUser steamUser = new SteamUser(steamId, parsedShareCode, authenticationCode);
                steamUserRepository.save(steamUser);
                
                event.reply("Registered :)")
                        .setEphemeral(true)
                        .queue();
            } else {
                // Send info
                // TODO: Improve UI
                String message = "👋 Hello! Choose an option:";
                Button steamIdButton = Button.link("https://steamcommunity.com/my", "\uD83D\uDC49 Steam ID");
                Button shareCodeButton = Button.link("https://help.steampowered.com/en/wizard/HelpWithGameIssue/?appid=730&issueid=128&ref=google.com", "\uD83D\uDC49 Share Code & Authentication Code");

                event.reply(message)
                        .addActionRow(steamIdButton, shareCodeButton)
                        .setEphemeral(true)
                        .queue();
            }
        }
    }
}
