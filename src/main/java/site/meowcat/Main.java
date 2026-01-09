package site.meowcat;

import discord4j.common.util.Snowflake;
import discord4j.core.DiscordClient;
import discord4j.core.event.domain.interaction.ButtonInteractionEvent;
import discord4j.core.event.domain.interaction.SelectMenuInteractionEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.component.ActionRow;
import discord4j.core.object.component.SelectMenu;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.gateway.intent.IntentSet;
import discord4j.rest.util.Permission;
import io.github.cdimascio.dotenv.Dotenv;
import reactor.core.publisher.Mono;
import site.meowcat.models.GuildSettings;
import com.fasterxml.jackson.core.type.TypeReference;
import site.meowcat.models.GuildSettings;
import com.fasterxml.jackson.core.type.TypeReference;
import java.util.HashMap;
import java.util.Map;
import java.io.File;
import java.io.IOException;


import site.meowcat.commands.*;
import site.meowcat.models.GuildSettings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static site.meowcat.LevelManager.*;

public class Main {

    public static final List<Command> commands = new ArrayList<>();


    static {
        commands.add(new Kick());
        commands.add(new Ban());
        commands.add(new Rank());
        commands.add(new Level());
        commands.add(new Leaderboard());
        commands.add(new SetLevelChannel());
        commands.add(new SetCommandPermission());
        loadUserData();        // XP
        loadGuildData();   // guild settings
    }

    public static void main(String[] args) {
        Dotenv dotenv = Dotenv.load();
        String token = dotenv.get("DISCORD_TOKEN");

        if (token == null || token.isBlank()) {
            throw new IllegalStateException("Missing DISCORD_TOKEN");
        }

        DiscordClient.create(token)
                .gateway()
                .setEnabledIntents(IntentSet.all())
                .withGateway(client -> {

                    client.updatePresence(
                            discord4j.core.object.presence.ClientPresence.online(
                                    discord4j.core.object.presence.ClientActivity.playing(">help")
                            )
                    ).subscribe();

                    // =====================
                    // MESSAGE HANDLER
                    // =====================
                    Mono<Void> handleMessages = client.on(MessageCreateEvent.class, event -> {
                        Message message = event.getMessage();

                        // ---------- XP HANDLING (FIXED) ----------
                        if (message.getAuthor().isPresent()
                                && !message.getAuthor().get().isBot()
                                && event.getGuildId().isPresent()) {

                            String guildId = event.getGuildId().get().asString();
                            String userId = message.getAuthor().get().getId().asString();

                            boolean leveledUp = LevelManager.addXp(guildId, userId);

                            if (leveledUp) {
                                int newLevel = LevelManager
                                        .getUserData(guildId, userId)
                                        .getLevel();

                                GuildSettings settings = LevelManager.getGuildSettings(guildId);

                                Mono<MessageChannel> channelMono;
                                if (settings != null && settings.getLevelUpChannelId() != null) {
                                    channelMono = client
                                            .getChannelById(Snowflake.of(settings.getLevelUpChannelId()))
                                            .cast(MessageChannel.class);
                                } else {
                                    channelMono = message.getChannel();
                                }

                                channelMono
                                        .flatMap(channel ->
                                                channel.createMessage(
                                                        "🎉 Congratulations <@" + userId +
                                                                ">, you've reached **Level " + newLevel + "**!"
                                                )
                                        )
                                        .subscribe();
                            }
                        }

                        // ---------- COMMAND HANDLING ----------
                        for (Command command : commands) {
                            if (message.getContent().toLowerCase()
                                    .startsWith(command.getTrigger().toLowerCase())) {

                                return message.getAuthorAsMember().flatMap(member -> {
                                    String guildId = event.getGuildId()
                                            .map(Snowflake::asString)
                                            .orElse(null);

                                    Permission requiredPermission = command.getDefaultPermission();
                                    String requiredRoleId = null;

                                    if (guildId != null) {
                                        GuildSettings settings = LevelManager.getGuildSettings(guildId);

                                        requiredRoleId = settings
                                                .getCommandRoles()
                                                .get(command.getTrigger());

                                        String overriddenPerm = settings
                                                .getCommandPermissions()
                                                .get(command.getTrigger());

                                        if (overriddenPerm != null) {
                                            try {
                                                requiredPermission = Permission.valueOf(overriddenPerm);
                                            } catch (IllegalArgumentException ignored) {}
                                        }
                                    }

                                    if (requiredRoleId != null) {
                                        String finalRoleId = requiredRoleId;
                                        boolean hasRole = member.getRoleIds()
                                                .stream()
                                                .anyMatch(id -> id.asString().equals(finalRoleId));

                                        return member.getBasePermissions().flatMap(perms -> {
                                            if (!hasRole && !perms.contains(Permission.ADMINISTRATOR)) {
                                                return message.getChannel()
                                                        .flatMap(c -> c.createMessage(
                                                                "❌ You don't have the required role."
                                                        ))
                                                        .then();
                                            }
                                            return command.execute(event);
                                        });
                                    }

                                    if (requiredPermission != null) {
                                        Permission finalPerm = requiredPermission;
                                        return member.getBasePermissions().flatMap(perms -> {
                                            if (!perms.contains(finalPerm)
                                                    && !perms.contains(Permission.ADMINISTRATOR)) {

                                                return message.getChannel()
                                                        .flatMap(c -> c.createMessage(
                                                                "❌ Missing permission: `" + finalPerm.name() + "`"
                                                        ))
                                                        .then();
                                            }
                                            return command.execute(event);
                                        });
                                    }

                                    return command.execute(event);
                                });
                            }
                        }

                        return Mono.empty();
                    }).then();

                    // =====================
                    // BUTTON HANDLER
                    // =====================
                    Mono<Void> handleButtons = client.on(ButtonInteractionEvent.class, event -> {
                        String id = event.getCustomId();

                        if (id.startsWith("setperm:reset:")) {
                            String trigger = id.substring("setperm:reset:".length());
                            String guildId = event.getInteraction()
                                    .getGuildId()
                                    .map(Snowflake::asString)
                                    .orElse(null);

                            if (guildId == null) return Mono.empty();

                            GuildSettings settings = LevelManager.getGuildSettings(guildId);
                            settings.getCommandRoles().remove(trigger);
                            LevelManager.saveGuildData();

                            return event.edit("✅ Permissions reset for `" + trigger + "`")
                                    .withComponents(new ArrayList<>());
                        }

                        return Mono.empty();
                    }).then();

                    // =====================
                    // SELECT MENU HANDLER
                    // =====================
                    Mono<Void> handleMenus = client.on(SelectMenuInteractionEvent.class, event -> {
                        String id = event.getCustomId();

                        if (id.equals("setperm:select_cmd")) {
                            String trigger = event.getValues().get(0);
                            return event.reply("Manage permissions for `" + trigger + "`:")
                                    .withEphemeral(true)
                                    .withComponents(
                                            ActionRow.of(
                                                    SelectMenu.ofRole("setperm:role:" + trigger)
                                                            .withPlaceholder("Assign a role")
                                            )
                                    );
                        }

                        if (id.startsWith("setperm:role:")) {
                            String trigger = id.substring("setperm:role:".length());
                            String roleId = event.getValues().get(0);
                            String guildId = event.getInteraction()
                                    .getGuildId()
                                    .map(Snowflake::asString)
                                    .orElse(null);

                            if (guildId == null) return Mono.empty();

                            GuildSettings settings = LevelManager.getGuildSettings(guildId);
                            settings.getCommandRoles().put(trigger, roleId);
                            LevelManager.saveGuildData();

                            return event.edit("✅ Role assigned successfully.")
                                    .withComponents(new ArrayList<>());
                        }

                        return Mono.empty();
                    }).then();

                    return Mono.when(handleMessages, handleButtons, handleMenus);
                })
                .block();
    }
}
