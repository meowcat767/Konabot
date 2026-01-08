package site.meowcat.commands;

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

        return message.getChannel().flatMap(channel -> {

            String userId;
            String username;

            if (!message.getUserMentions().isEmpty()) {
                var user = message.getUserMentions().get(0);
                userId = user.getId().asString();
                username = user.getUsername();
            } else {
                var author = message.getAuthor().orElse(null);
                if (author == null) return Mono.empty();
                userId = author.getId().asString();
                username = author.getUsername();
            }

            UserData data = LevelManager.getUserData(userId);

            int level = data.getLevel();
            long xp = data.getXp();
            long nextLevelXp = data.getXpForLevel(level + 1);
            long currentLevelXp = data.getXpForLevel(level);
            long progress = xp - currentLevelXp;
            long needed = nextLevelXp - currentLevelXp;

            if (needed <= 0) needed = 1; // Prevent division by zero

            int barLength = 10;
            int filled = (int) ((double) progress / needed * barLength);

            filled = Math.max(0, Math.min(barLength, filled));

            String bar = "█".repeat(filled) + "░".repeat(barLength - filled);

            return channel.createMessage(
                    "📊 **" + username + "'s Level**\n" +
                            "🏆 Level: **" + level + "**\n" +
                            "✨ XP: **" + xp + "**\n" +
                            "📈 Progress: `" + bar + "` " +
                            "(" + progress + " / " + needed + ")"
            ).then();
        });
    }
}
