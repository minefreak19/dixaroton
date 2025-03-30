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
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalField;
import java.util.EnumSet;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

// TODO: Add a README
public class Dixaroton {
    private static ExarotonClient exarotonClient;
    public static final long DISCORD_MC_CHANNEL_ID = 1355846009581863033L;
    public static final long DISCORD_SELF_ID = 1355614585792626932L;

    public static void main(String[] args) {
        String discordApiToken = System.getenv("DISCORD_API_TOKEN");
        String exarotonApiKey = System.getenv("EXAROTON_API_KEY");

        final var dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.of("UTC"));

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

        System.out.println("Adding console subscriber...");
        server.addConsoleSubscriber(line -> {
            System.out.println("Console subscriber called.");
            // This probably shouldn't happen but just in case
            if (line.length() < 22) {
                System.out.printf("Ignoring short line `%s`\n", line);
                return;
            }

            // We don't want the space here
            String timestamp = line.substring(0, 21);
            if (!timestamp.matches("\\[\\d\\d\\d\\d-\\d\\d-\\d\\d [0-2][0-9]:[0-5][0-9]:[0-6][0-9]]")) {
                System.out.printf("No regex match for `%s`\n", timestamp);
                return;
            }
            Instant cutoff = Instant.now().minusSeconds(2);
            System.out.printf("Trying to parse lineTime: `%s`\n", timestamp.substring(1, 20));
            Instant lineTime = Instant.from(dateTimeFormatter.parse(timestamp.substring(1, 20)));
            System.out.printf("Cutoff: %s\nlineTime: %s\n", cutoff, lineTime);

            if (lineTime.isBefore(cutoff)) {
                System.out.println("Ignoring (too old)...");
                return;
            }

            // Exclude the datetime info
            line = line.substring(22);

            // TODO: Find a way to also report player join/leave messages to Discord
            // TODO: Ping someone in minecraft when they're mentioned by MC username on Discord
            // TODO: Improve emoji/embed/image dispay (e.g. say <image> if a message contains an image)
            // TODO: Bot can't handle "/say" messages from the server console
            // This occurs at the beginning of all chat messages
            if (!line.startsWith("[Server thread/INFO]: [Not Secure]")) {
                System.out.printf("Ignoring non-chat line `%s`\n", line);
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