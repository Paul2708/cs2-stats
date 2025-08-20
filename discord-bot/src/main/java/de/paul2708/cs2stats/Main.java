package de.paul2708.cs2stats;

import de.paul2708.cs2stats.discord.*;
import de.paul2708.cs2stats.discord.history.HistoryCommand;
import de.paul2708.cs2stats.repository.DatabaseConnector;
import de.paul2708.cs2stats.repository.MatchRepository;
import de.paul2708.cs2stats.repository.SteamUserRepository;
import de.paul2708.cs2stats.service.MatchService;
import de.paul2708.cs2stats.steam.DemoProviderClient;
import de.paul2708.cs2stats.steam.SteamClient;
import io.github.cdimascio.dotenv.Dotenv;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

public class Main {
    public static void main(String[] args) {
        // Load environment variables
        Dotenv dotenv = Dotenv.configure()
                .ignoreIfMissing()
                .load();

        // Connect to database
        DatabaseConnector databaseConnector = new DatabaseConnector();
        databaseConnector.connect(dotenv);

        // Create services
        SteamUserRepository steamUserRepository = new SteamUserRepository();
        MatchRepository matchRepository = new MatchRepository();

        SteamClient steamClient = new SteamClient(dotenv);
        DemoProviderClient demoProviderClient = new DemoProviderClient(dotenv);

        MatchService matchService = new MatchService(steamUserRepository, matchRepository, steamClient, demoProviderClient);

        // Create Discord bot
        JDA jda = JDABuilder.createDefault(dotenv.get("BOT_TOKEN")).build();

        jda.updateCommands().addCommands(
                Commands.slash("info", "Show registration information."),
                Commands.slash("register", "Register your Steam account.")
                        .addOption(OptionType.STRING, "steamid", "Your Steam ID", true)
                        .addOption(OptionType.STRING, "sharecode", "Your latest share code (i.e., CSGO-...).", true)
                        .addOption(OptionType.STRING, "authenticationcode", "Your authentication code to download demos.", true),
                Commands.slash("ranks", "Plot the rank history of all registered users."),
                Commands.slash("fetch", "Fetch a match manually.")
                        .addOption(OptionType.STRING, "sharecode", "Match share code (i.e., CSGO-...) to be fetched.", true),
                Commands.slash("history", "Show the history for a given Steam user.")
                        .addOption(OptionType.USER, "target", "Select the Discord user", true)
        ).queue();
        jda.addEventListener(new InfoCommand());
        jda.addEventListener(new RegisterCommand(steamUserRepository, matchService, steamClient));
        jda.addEventListener(new RankHistoryCommand(steamUserRepository, matchRepository));
        jda.addEventListener(new FetchCommand(matchService));
        jda.addEventListener(new HistoryCommand(steamUserRepository, matchRepository));

        // Run update task
        matchService.fetchLatestMatchesPeriodically();
    }
}