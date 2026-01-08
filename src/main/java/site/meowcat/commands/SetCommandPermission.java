package site.meowcat.commands;

import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.component.ActionRow;
import discord4j.core.object.component.SelectMenu;
import discord4j.core.object.component.SelectMenu.Option;
import discord4j.core.object.entity.Message;
import discord4j.rest.util.Permission;
import reactor.core.publisher.Mono;
import site.meowcat.Main;

import java.util.ArrayList;
import java.util.List;

public class SetCommandPermission implements Command {
    @Override
    public String getTrigger() {
        return ">setperm";
    }

    @Override
    public Permission getDefaultPermission() {
        return Permission.MANAGE_GUILD;
    }

    @Override
    public Mono<Void> execute(MessageCreateEvent event) {
        Message message = event.getMessage();
        
        List<Option> options = new ArrayList<>();

        for (Command cmd : Main.commands) {
            String trigger = cmd.getTrigger();
            // Don't allow setting permissions for setperm itself to avoid locking out
            if (trigger.equals(getTrigger())) continue;
            
            options.add(Option.of(trigger, trigger));
        }

        return message.getChannel().flatMap(channel ->
            channel.createMessage("Select a command to manage its permissions:")
                .withComponents(ActionRow.of(
                    SelectMenu.of("setperm:select_cmd", options)
                        .withPlaceholder("Choose a command...")
                ))
        ).then();
    }
}
