package site.meowcat.commands;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.rest.util.Permission;
import reactor.core.publisher.Mono;

public class Kick implements Command {

    @Override
    public String getTrigger() {
        return ">k";
    }

    @Override
    public Permission getDefaultPermission() {
        return Permission.KICK_MEMBERS;
    }

    @Override
    public Mono<Void> execute(MessageCreateEvent event) {
        Message message = event.getMessage();
        String content = message.getContent();
        // Split into: >k <mention> <reason :op:>
        String[] parts = content.split("\\s+", 3);
        String reason = parts.length >= 3 ? parts[2] : "No reason provided.";
        MessageChannel channel = message.getChannel().block();
        if (channel == null) return Mono.empty();

        return message.getAuthorAsMember().flatMap(author -> {

            return message.getGuildId()
                    .map(guildId -> {

                        // Check user mention
                        if (message.getUserMentions().isEmpty()) {
                            return channel.createMessage("❌ Please mention a user to kick.").then();
                        }

                        // Target member
                        return message.getUserMentions().get(0)
                                .asMember(guildId)
                                .flatMap(target ->

                                        // Get bot member
                                        event.getClient().getSelf()
                                                .flatMap(botUser -> botUser.asMember(guildId))
                                                .flatMap(botMember ->

                                                        // Collect bot roles positions
                                                        botMember.getRoles()
                                                                .flatMap(role -> role.getPosition())
                                                                .collectList()
                                                                .flatMap(botPositions ->

                                                                        // Collect target roles positions
                                                                        target.getRoles()
                                                                                .flatMap(role -> role.getPosition())
                                                                                .collectList()
                                                                                .flatMap(targetPositions -> {

                                                                                    int botMax = botPositions.stream().mapToInt(Integer::intValue).max().orElse(0);
                                                                                    int targetMax = targetPositions.stream().mapToInt(Integer::intValue).max().orElse(0);

                                                                                    if (botMax <= targetMax) {
                                                                                        return channel.createMessage(
                                                                                                "❌ I cannot kick this user because their role is higher or equal to mine."
                                                                                        ).then();
                                                                                    }

                                                                                    // Kick target
                                                                                    return target.kick(reason)
                                                                                            .doOnSuccess(unused -> System.out.println("Kicked: " + target.getUsername()))
                                                                                            .doOnError(e -> System.err.println("Kick failed: " + e.getMessage()))
                                                                                            .then(channel.createMessage("✅ User has been kicked!"));
                                                                                })
                                                                )
                                                )
                                )
                                .onErrorResume(e -> channel.createMessage("❌ Failed to kick user: " + e.getMessage()).then());
                    })
                    .orElseGet(() -> channel.createMessage("❌ This command can only be used in a server.").then());
        }).then(); // Top-Level Mono<Void>
    }
}
