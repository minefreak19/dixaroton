package me.minefreak19;

import com.exaroton.api.server.Server;
import com.exaroton.api.server.ServerStatus;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class SlashCommandListener extends ListenerAdapter {
    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        switch (event.getName()) {
            case "status" -> {
                List<Server> servers;
                try {
                    servers = Main.getExarotonClient().getServers().join();
                } catch (IOException e) {
                    event.reply("Sorry, an error occurred while accessing Exaroton status: ```" + e.getMessage() + "```")
                            .setEphemeral(true)
                            .queue();
                    e.printStackTrace(System.err);
                    return;
                }

                var content = new StringBuilder("# Exaroton Status");
                for (Server server: servers) {
                    var statusText = server.getStatus().toString();
                    var statusEmoji = switch (server.getStatus()) {
                        case OFFLINE -> "⬛️";
                        case ONLINE -> "🟩";
                        case PREPARING, LOADING, STARTING -> "🕣";
                        case CRASHED -> "🟥";
                        default -> "🟡";
                    };

                    content.append(String.format("\n## %s `%s` (%s)", statusEmoji, server.getName(), statusText));
                }
                event.replyEmbeds(new EmbedBuilder()
                                .setDescription(content.toString().trim())
                                .build())
                        .queue();
            }
        }
    }
}
