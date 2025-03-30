package me.minefreak19.dixaroton;

import com.exaroton.api.server.Server;
import com.exaroton.api.server.ServerStatus;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.io.IOException;
import java.util.List;

public class SlashCommandListener extends ListenerAdapter {
    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        // TODO: Add a central Listener in the main class for server events and maintain server status and player list in a cache for this response
        // TODO: Implement a persistent message published in a set channel which is updated upon receiving a new status, rather than through a command
        switch (event.getName()) {
            case "status" -> {
                List<Server> servers;
                try {
                    servers = Dixaroton.getExarotonClient().getServers().join();
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

                    content.append(String.format("\n## %s `%s`: %s", statusEmoji, server.getName(), statusText));
                    if (server.getStatus() == ServerStatus.ONLINE) {
                        content.append(String.format(" (%d/%d)", server.getPlayerInfo().getCount(), server.getPlayerInfo().getMax()));
                        System.out.println(server.getPlayerInfo().getList());
                        for (String name : server.getPlayerInfo().getList()) {
                            content.append("\n\t").append(name);
                        }
                    }
                }
                event.replyEmbeds(new EmbedBuilder()
                                .setDescription(content.toString().trim())
                                .build())
                        .setEphemeral(true)
                        .queue();
            }
        }
    }
}
