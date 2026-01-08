package site.meowcat;
import discord4j.core.DiscordClient;
import discord4j.core.DiscordClientBuilder;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;

import discord4j.core.object.entity.Message;
import io.github.cdimascio.dotenv.*;
import io.github.cdimascio.dotenv.Dotenv;
import reactor.core.publisher.Mono;


import site.meowcat.commands.Ban;
import site.meowcat.commands.Command;
import site.meowcat.commands.Kick;
import site.meowcat.commands.Rank;
import site.meowcat.commands.Leaderboard;

import java.util.ArrayList;
import java.util.List;

public class Main {
    private static final List<Command> commands = new ArrayList<>();

    static {
        commands.add(new Kick());
        commands.add(new Ban());
        commands.add(new Rank());
        commands.add(new Leaderboard());
    }

    public static void main(String[] args) throws InterruptedException {
        Dotenv dotenv = Dotenv.load(); // load a .env file containing the token
        String token = dotenv.get("DISCORD_TOKEN");

        System.out.println("Token loaded: '" + token + "'");
        if (token == null || token.isBlank()) {
            throw new IllegalStateException(); // throw this in case of missing token
        }

        DiscordClient.create(token)
                .withGateway(client ->
                        client.on(MessageCreateEvent.class, event -> {
                            Message message = event.getMessage();
                            if (message.getAuthor().isPresent() && !message.getAuthor().get().isBot()) {
                                String userId = message.getAuthor().get().getId().asString();
                                boolean leveledUp = LevelManager.addXp(userId);
                                if (leveledUp) {
                                    int newLevel = LevelManager.getUserData(userId).getLevel();
                                    message.getChannel()
                                            .flatMap(channel -> channel.createMessage("🎉 Congratulations <@" + userId + ">, you've reached level **" + newLevel + "**!"))
                                            .subscribe();
                                }
                            }

                            if (message.getAuthor().isPresent()) {
                                System.out.println("Message from: " + message.getAuthor());
                            }

                            for (Command command : commands) {
                                if (message.getContent().toLowerCase().startsWith(command.getTrigger().toLowerCase())) {
                                    return command.execute(event);
                                }
                            }

                            if (message.getContent().equalsIgnoreCase(">ping")) {
                                return message.getChannel()
                                        .flatMap(channel -> channel.createMessage("Pong!"))
                                        .then();
                            }

                            return Mono.empty();
                        }))
                .block();
    }}
