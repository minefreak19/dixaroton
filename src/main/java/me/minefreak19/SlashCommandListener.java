package me.minefreak19;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class SlashCommandListener extends ListenerAdapter {
    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        switch (event.getName()) {
            case "status" -> {
                event.reply("This command is unfinished!")
                        .setEphemeral(true)
                        .queue();
            }
        }
    }
}
