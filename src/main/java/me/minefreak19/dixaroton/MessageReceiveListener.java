package me.minefreak19.dixaroton;

import com.exaroton.api.server.Server;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.io.IOException;
import java.util.List;

public class MessageReceiveListener extends ListenerAdapter {
    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().getIdLong() == Dixaroton.DISCORD_SELF_ID) return;
        if (event.getChannel().getIdLong() != Dixaroton.DISCORD_MC_CHANNEL_ID) return;

        System.out.printf("%#s: %s\n",
                event.getAuthor(),
                event.getMessage().getContentDisplay());

        List<Server> servers;
        try {
            servers = Dixaroton.getExarotonClient().getServers().join();
        } catch (IOException e) {
            e.printStackTrace(System.err);
            return;
        }

        for (var server : servers) {
            try {
                String content = minecraftSanitize(event.getMessage().getContentDisplay());
                server.executeCommand(String.format("tellraw @a {\"text\":\"§r[§9§lDiscord§r] <%s> %s\"}", event.getAuthor().getEffectiveName(), content)).join();
            } catch (IOException e) {
                e.printStackTrace(System.err);
            }
        }

    }

    private static String minecraftSanitize(String s) {
        return s.replace("§", "")
                .replace("\\","\\\\")
                .replace("\"", "\\\"");
    }
}
