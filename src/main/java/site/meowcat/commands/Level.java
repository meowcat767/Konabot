package site.meowcat.commands;

import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import reactor.core.publisher.Mono;
import site.meowcat.LevelManager;
import site.meowcat.models.UserData;

public class Level implements Command {

    @Override
    public String getTrigger() {
        return ">level";
    }

    @Override
        public Mono<Void> execute(MessageCreateEvent event) {
            Message message = event.getMessage();

            if (message.getAuthor().isEmpty() || message.getAuthor().get().isBot()) {
                return Mono.empty();
            }

            String userId = message.getAuthor().get().getId().asString();

            String guildId = event.getGuildId()
                    .map(Snowflake::asString)
                    .orElse(null);
            if (guildId == null) {
                return message.getChannel()
                        .flatMap(channel -> channel.createMessage("❌ This command can only be used in a server."))
                        .then();
            }

            UserData data = LevelManager.getUserData(guildId, userId);

            return message.getChannel()
                    .flatMap(channel -> channel.createMessage(
                            "You are Level " + data.getLevel() + " with " + data.getXp() + " XP."
                    ))
                    .then();
        }

}
