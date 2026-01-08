package site.meowcat.commands;

import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.rest.util.Permission;
import reactor.core.publisher.Mono;
import site.meowcat.LevelManager;
import site.meowcat.models.GuildSettings;

import java.util.Arrays;
import java.util.stream.Collectors;

public class SetCommandPermission implements Command {
    @Override
    public String getTrigger() {
        return ">setperm";
    }

    @Override
    public Permission getDefaultPermission() {
        return Permission.MANAGE_GUILD;
    }

    @Override
    public Mono<Void> execute(MessageCreateEvent event) {
        Message message = event.getMessage();
        String content = message.getContent();
        String[] parts = content.split("\\s+");

        if (parts.length < 3) {
            return message.getChannel().flatMap(channel ->
                    channel.createMessage("❌ Usage: `>setperm <command_trigger> <permission_name>`\n" +
                            "Example: `>setperm >b MANAGE_MESSAGES` or `>setperm >b DEFAULT` to reset.")
            ).then();
        }

        String targetTrigger = parts[1];
        String permName = parts[2].toUpperCase();

        String guildId = event.getGuildId().map(Snowflake::asString).orElse(null);
        if (guildId == null) return Mono.empty();

        GuildSettings settings = LevelManager.getGuildSettings(guildId);

        if (permName.equals("DEFAULT")) {
            settings.getCommandPermissions().remove(targetTrigger);
            LevelManager.saveGuildData();
            return message.getChannel().flatMap(channel ->
                    channel.createMessage("✅ Permission for `" + targetTrigger + "` has been reset to default.")
            ).then();
        }

        try {
            Permission permission = Permission.valueOf(permName);
            settings.getCommandPermissions().put(targetTrigger, permission.name());
            LevelManager.saveGuildData();
            return message.getChannel().flatMap(channel ->
                    channel.createMessage("✅ Permission for `" + targetTrigger + "` has been set to `" + permission.name() + "`.")
            ).then();
        } catch (IllegalArgumentException e) {
            return message.getChannel().flatMap(channel ->
                    channel.createMessage("❌ Invalid permission name. Valid permissions include: `BAN_MEMBERS`, `KICK_MEMBERS`, `MANAGE_MESSAGES`, `ADMINISTRATOR`, etc.")
            ).then();
        }
    }
}
