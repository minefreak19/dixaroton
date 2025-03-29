package me.minefreak19;

import com.exaroton.api.ExarotonClient;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.GatewayIntent;

import java.util.EnumSet;

public class Main {
    private static ExarotonClient exarotonClient;

        public static void main(String[] args) {
            String discordApiToken = System.getenv("DISCORD_API_TOKEN");
            String exarotonApiKey = System.getenv("EXAROTON_API_KEY");

            exarotonClient = new ExarotonClient(exarotonApiKey);

            JDA jda = JDABuilder.createLight(discordApiToken, EnumSet.of(GatewayIntent.GUILD_MESSAGES, GatewayIntent.MESSAGE_CONTENT))
                    .addEventListeners(new SlashCommandListener())
                .addEventListeners(new MessageReceiveListener())
                .build();

        jda.updateCommands()
                .addCommands(
                        Commands.slash("status", "Get server status")
                )
                .queue();
    }

    public static ExarotonClient getExarotonClient() {
        return exarotonClient;
    }
}