package site.meowcat;
import discord4j.core.DiscordClient;
import discord4j.core.DiscordClientBuilder;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;

import discord4j.core.object.entity.Message;
import io.github.cdimascio.dotenv.*;
import io.github.cdimascio.dotenv.Dotenv;
import reactor.core.publisher.Mono;


public class Main {
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
                            if (message.getAuthor().isPresent()) {
                                System.out.println("Message from: " + message.getAuthor());
                            }

                            if (message.getContent().equalsIgnoreCase("!ping")) {
                                return message.getChannel()
                                        .flatMap(channel -> channel.createMessage("Pong!"));
                            }

                            return Mono.empty();
                        }))
                .block();
    }}
