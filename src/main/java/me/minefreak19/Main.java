package me.minefreak19;

import com.exaroton.api.ExarotonClient;
import com.exaroton.api.server.Server;
import com.exaroton.api.server.ServerStatus;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.GatewayIntent;

import java.io.IOException;
import java.util.EnumSet;
import java.util.List;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    private static ExarotonClient exarotonClient;

    public static void main(String[] args) throws IOException {
        String discordApiToken = System.getenv("DISCORD_API_TOKEN");
        String exarotonApiKey = System.getenv("EXAROTON_API_KEY");

        exarotonClient = new ExarotonClient(exarotonApiKey);
        List<Server> servers = exarotonClient.getServers().join();
        for (Server server: servers) {
            System.out.println(server.getId() + ":" + server.getAddress());
            System.out.println(server.getStatus());

            if (server.hasStatus(ServerStatus.ONLINE)) {
                System.out.println("Server is online!");
            }
            else if (server.hasStatus(ServerStatus.OFFLINE)) {
                System.out.println("Server is offline!");
            }
            else if (server.hasStatus(ServerStatus.PREPARING, ServerStatus.LOADING, ServerStatus.STARTING)) {
                System.out.println("Server is starting!");
            }
        }

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