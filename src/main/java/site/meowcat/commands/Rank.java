package site.meowcat.commands;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.User;
import reactor.core.publisher.Mono;
import site.meowcat.LevelManager;
import site.meowcat.models.UserData;

public class Rank implements Command {
    @Override
    public String getTrigger() {
        return ">rank";
    }

    @Override
    public Mono<Void> execute(MessageCreateEvent event) {
        User target = event.getMessage().getUserMentions().isEmpty() 
            ? event.getMessage().getAuthor().orElse(null) 
            : event.getMessage().getUserMentions().get(0);

        if (target == null) return Mono.empty();

        UserData data = LevelManager.getUserData(target.getId().asString());
        
        return event.getMessage().getChannel().flatMap(channel -> 
            channel.createMessage(String.format(
                "📊 **%s's Progress**\nLevel: %d\nXP: %d",
                target.getUsername(), data.getLevel(), data.getXp()
            ))
        ).then();
    }
}
