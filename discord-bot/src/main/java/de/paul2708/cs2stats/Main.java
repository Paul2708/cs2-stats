package de.paul2708.cs2stats;

import io.github.cdimascio.dotenv.Dotenv;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

public class Main {
    public static void main(String[] args) {
        Dotenv dotenv = Dotenv.load();

        JDA jda = JDABuilder.createDefault(dotenv.get("BOT_TOKEN")).build();

        jda.updateCommands().addCommands(
                Commands.slash("register", "Repeats messages back to you.")
                        .addOption(OptionType.STRING, "steamid", "Your Steam ID", false)
                        .addOption(OptionType.STRING, "sharecode", "Your latest share code (i.e., CSGO-...).", false)
                        .addOption(OptionType.STRING, "authenticationcode", "Your authentication code to download demos.", false)
        ).queue();
        jda.addEventListener(new RegisterCommand());
    }
}