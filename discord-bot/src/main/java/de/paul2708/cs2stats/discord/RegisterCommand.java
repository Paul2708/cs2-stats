package de.paul2708.cs2stats.discord;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

public class RegisterCommand extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (event.getName().equals("register")) {
            OptionMapping steamId = event.getOption("steamid");
            OptionMapping shareCode = event.getOption("sharecode");
            OptionMapping authenticationCode = event.getOption("authenticationcode");

            if (steamId != null && shareCode != null && authenticationCode != null) {
                // Register
                event.reply("You send: " + steamId.getAsString() + ", " + shareCode.getAsString() + ", " + authenticationCode.getAsString())
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
