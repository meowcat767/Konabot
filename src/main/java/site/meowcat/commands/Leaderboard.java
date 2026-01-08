package site.meowcat.commands;

import discord4j.core.event.domain.message.MessageCreateEvent;
import reactor.core.publisher.Mono;
import site.meowcat.LevelManager;
import site.meowcat.models.UserData;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Leaderboard implements Command {
    @Override
    public String getTrigger() {
        return ">leaderboard";
    }

    @Override
    public Mono<Void> execute(MessageCreateEvent event) {
        Map<String, UserData> allData = LevelManager.getAllUserData();
        
        List<Map.Entry<String, UserData>> sorted = allData.entrySet().stream()
            .sorted(Comparator.comparingLong((Map.Entry<String, UserData> e) -> e.getValue().getXp()).reversed())
            .limit(10)
            .collect(Collectors.toList());

        StringBuilder sb = new StringBuilder("🏆 **Leaderboard**\n");
        int rank = 1;
        for (Map.Entry<String, UserData> entry : sorted) {
            sb.append(String.format("%d. <@%s> - Level %d (%d XP)\n", 
                rank++, entry.getKey(), entry.getValue().getLevel(), entry.getValue().getXp()));
        }

        if (sorted.isEmpty()) {
            sb.append("No data yet!");
        }

        return event.getMessage().getChannel()
            .flatMap(channel -> channel.createMessage(sb.toString()))
            .then();
    }
}
