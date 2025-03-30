package me.minefreak19.dixaroton;

import com.exaroton.api.ExarotonClient;
import com.exaroton.api.server.Server;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.GatewayIntent;

import java.io.IOException;
import java.util.EnumSet;
import java.util.Objects;

// TODO: Add a README
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
            // TODO: Replace going through the server list with just using the ID directly
            server = exarotonClient.getServers().join().getFirst();
        } catch (IOException e) {
            e.printStackTrace(System.err);
            return;
        }

        // TODO: Doing it this way results in exaroton resending the entire chat history if the bot crashes or restarts; there may be a better way which ensures messages don't get duplicated on Discord
        server.addConsoleSubscriber(line -> {
            if (line.length() < 11) return;
            // Exclude the timestamp
            line = line.substring(11);

            // TODO: Find a way to also report player join/leave messages to Discord
            // This occurs at the beginning of all chat messages
            if (!line.startsWith("[Server thread/INFO]: [Not Secure]")) {
                return;
            }

            // Exclude the thread info and [Not Secure], a space, and the < before the username
            line = line.substring(36);
            String playerName, message;
            {
               int i = line.indexOf('>');
               playerName = line.substring(0, i);

                if (i + 2 > line.length()) {
                    // TODO: Panicking here might not be a good idea
                    throw new RuntimeException(String.format("Malformed console output line (expected '>' before message): `%s`\n", line));
                }
                // TODO: Good idea to escape this (and the username) for Discord markdown
                message = line.substring(i + 2);
            }

            Objects.requireNonNull(jda.getChannelById(TextChannel.class, DISCORD_MC_CHANNEL_ID))
                    .sendMessageEmbeds(new EmbedBuilder()
                            .setTitle(playerName)
                            .setDescription(message)
                            .build())
                    .queue();
        });
    }

    public static ExarotonClient getExarotonClient() {
        return exarotonClient;
    }
}