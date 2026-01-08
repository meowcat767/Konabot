package site.meowcat.commands;

import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.message.MessageCreateEvent;
import reactor.core.publisher.Flux;
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

        if (sorted.isEmpty()) {
            return event.getMessage().getChannel()
                .flatMap(channel -> channel.createMessage("🏆 **Leaderboard**\nNo data yet!"))
                .then();
        }

        return Flux.fromIterable(sorted)
            .concatMap(entry -> event.getClient().getUserById(Snowflake.of(entry.getKey()))
                .map(user -> user.getUsername())
                .onErrorReturn("Unknown User (" + entry.getKey() + ")")
                .map(username -> new LeaderboardEntry(username, entry.getValue())))
            .collectList()
            .flatMap(entries -> {
                StringBuilder sb = new StringBuilder("🏆 **Leaderboard**\n");
                for (int i = 0; i < entries.size(); i++) {
                    LeaderboardEntry entry = entries.get(i);
                    sb.append(String.format("%d. %s - Level %d (%d XP)\n", 
                        i + 1, entry.username, entry.data.getLevel(), entry.data.getXp()));
                }
                return event.getMessage().getChannel()
                    .flatMap(channel -> channel.createMessage(sb.toString()));
            })
            .then();
    }

    private static class LeaderboardEntry {
        final String username;
        final UserData data;

        LeaderboardEntry(String username, UserData data) {
            this.username = username;
            this.data = data;
        }
    }
}
