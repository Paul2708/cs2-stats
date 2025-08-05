package de.paul2708.cs2stats.discord;

import de.paul2708.cs2stats.entity.SteamUser;
import de.paul2708.cs2stats.repository.MatchRepository;
import de.paul2708.cs2stats.repository.SteamUserRepository;
import de.paul2708.cs2stats.steam.DemoProviderClient;
import de.paul2708.cs2stats.steam.Match;
import de.paul2708.cs2stats.steam.ShareCode;
import de.paul2708.cs2stats.steam.SteamClient;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RegisterCommand extends ListenerAdapter {

    private final SteamUserRepository steamUserRepository;
    private final MatchRepository matchRepository;

    private final SteamClient steamClient;
    private final DemoProviderClient demoProviderClient;

    private final ExecutorService executorService;

    public RegisterCommand(SteamUserRepository steamUserRepository, MatchRepository matchRepository, SteamClient steamClient, DemoProviderClient demoProviderClient) {
        this.steamUserRepository = steamUserRepository;
        this.matchRepository = matchRepository;
        this.steamClient = steamClient;
        this.demoProviderClient = demoProviderClient;

        this.executorService = Executors.newCachedThreadPool();
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

                SteamUser steamUser = new SteamUser(steamId, parsedShareCode, authenticationCode, parsedShareCode);
                steamUserRepository.create(steamUser);

                fetchPreviousMatches(steamUser);

                event.reply("Registered :) We are fetching your previous games. This may take a while.")
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

    private void fetchPreviousMatches(SteamUser steamUser) {
        this.executorService.submit(() -> {
            try {
                steamClient.requestConsecutiveCodes(steamUser.steamId(), steamUser.authenticationCode(), steamUser.lastKnownShareCode().shareCode(),
                        shareCode -> {
                            if (matchRepository.findMatchById(shareCode.matchId()).isPresent()) {
                                System.out.println("Skipped match because already stored");
                                return;
                            }

                            try {
                                Match match = demoProviderClient.requestMatch(shareCode);
                                System.out.println(match);

                                matchRepository.create(match);
                            } catch (Exception e) {
                                System.out.println("Failed to fetch previous matches");
                                e.printStackTrace();
                            }
                        });
            } catch (IOException | InterruptedException e) {
                System.out.println("Failed to fetch previous matches");
                e.printStackTrace();
            }
        });
    }
}
