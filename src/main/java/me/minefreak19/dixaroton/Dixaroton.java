package me.minefreak19.dixaroton;

import com.exaroton.api.ExarotonClient;
import com.exaroton.api.server.Server;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.GatewayIntent;

import java.io.IOException;
import java.util.EnumSet;
import java.util.Objects;

public class Dixaroton {
    private static ExarotonClient exarotonClient;
    public static final long DISCORD_MC_CHANNEL_ID = 1355668810119512214L;
    public static final long DISCORD_SELF_ID = 1355614585792626932L;

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

        Server server;
        try {
            server = exarotonClient.getServers().join().getFirst();
        } catch (IOException e) {
            e.printStackTrace(System.err);
            return;
        }
        // TODO: Replace going through the server list with just using this ID directly
        System.out.println(server.getId());
        server.addConsoleSubscriber(line -> Objects.requireNonNull(jda.getChannelById(TextChannel.class, DISCORD_MC_CHANNEL_ID))
                .sendMessage(line)
                .queue());
    }

    public static ExarotonClient getExarotonClient() {
        return exarotonClient;
    }
}