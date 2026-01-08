package site.meowcat.commands;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.spec.legacy.LegacyBanQuerySpec;
import discord4j.rest.util.Permission;
import reactor.core.publisher.Mono;

import java.time.Duration; // whack me that time
public class Ban implements Command {
    /**
     * This command bans the mentioned user.
     * We use ">b [user] [reason]
     * @return
     */
    @Override
    public String getTrigger() {
        return ">b";
    }

    @Override
    public Mono<Void> execute(MessageCreateEvent event) {
        Message message = event.getMessage();
        String content = message.getContent();
        MessageChannel channel = message.getChannel().block();
        if (channel == null) return Mono.empty();
        String[] parts = content.split("\\s+", 3); // note: we use regex here
        String reason = parts.length >= 3 ? parts[2] : "No reason provided.";
        return message.getAuthorAsMember().flatMap(author ->
                message.getGuild().flatMap(guild -> {
                    // perm check
                    if (!author.getBasePermissions().block().contains(Permission.BAN_MEMBERS)) {
                        return channel.createMessage("❌ You don't have permissions to ban this member!");
                    }
                    // must mention someone... check
                    if (message.getUserMentions().isEmpty()) {
                        return channel.createMessage("❌ You must mention someone!");
                    }
                    // lowk my first time just using "var", lets hope it works...
                    var user  = message.getUserMentions().get(0);
                    return guild.ban(user.getId(), spec ->
                                    spec.setReason(reason)
                                            .setDeleteMessageDays(1)
                            )
                            .doOnSuccess(unused ->
                                    System.out.println("Banned: " + user.getUsername() + " | Reason: " + reason))
                            .then(channel.createMessage(
                                    "🔨 **" + user.getUsername() + "** has been banned.\n📝 Reason: " + reason
                            ));
                })
        ).then();
                        }}
