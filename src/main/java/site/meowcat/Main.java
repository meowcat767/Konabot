package site.meowcat;
import com.discord4j.*;
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
        String token = dotenv.get("DISCORD_TOKEN"); // read the token from the .env we just loaded
        if (token == null || token.isBlank()) {
            throw new IllegalStateException("DISCORD_TOKEN IS MISSING"); // throw this in case of missing token
        }

        DiscordClient.create(token)
                .withGateway(client ->
                        client.on(MessageCreateEvent.class, event -> {
                            Message message = event.getMessage();

                            if (message.getContent().equalsIgnoreCase("!ping")) {
                                return message.getChannel()
                                        .flatMap(channel -> channel.createMessage("Pong!"));
                            }

                            return Mono.empty();
                        }))
                .block();
    }}
