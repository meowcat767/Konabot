package site.meowcat.commands;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import reactor.core.publisher.Mono;

public class help {

    public Mono<Void> handle(MessageCreateEvent event) {
        Message message = event.getMessage();

        if (message.getContent().equalsIgnoreCase(">help")) {
            return message.getChannel()
                    .flatMap(channel -> channel.createMessage("# Commands \n ## Moderation Commands: \n >k - kick, >b ban \n ## Level Commands: \n >leaderboard - open the leaderboard, >level - list user level \n ## Admin commands: \n >setperm - set command perms, >setlevelchan - set level logging channel"))
                    .then();
        }

        return Mono.empty();
    }
}
