package site.meowcat.commands;

import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.rest.util.Permission;
import reactor.core.publisher.Mono;
import site.meowcat.LevelManager;
import site.meowcat.models.GuildSettings;

public class SetLevelChannel implements Command {

    @Override
    public String getTrigger() {
        return ">setlevelchan";
    }

    @Override
    public Permission getDefaultPermission() {
        return Permission.MANAGE_GUILD;
    }

    @Override
    public Mono<Void> execute(MessageCreateEvent event) {
        Message message = event.getMessage();
        
        return message.getAuthorAsMember().flatMap(member -> {
                String guildId = event.getGuildId().map(Snowflake::asString).orElse(null);
                if (guildId == null) {
                    return message.getChannel().flatMap(channel -> 
                        channel.createMessage("❌ This command can only be used in a server.")
                    ).then();
                }

                if (!message.getContent().contains("<#") && !message.getContent().contains("reset")) {
                    return message.getChannel().flatMap(channel -> 
                        channel.createMessage("❌ Please mention a channel, e.g., `>setlevelchannel #levels`. Use `>setlevelchannel reset` to use the current channel.")
                    ).then();
                }

                GuildSettings settings = LevelManager.getGuildSettings(guildId);
                if (message.getContent().contains("reset")) {
                    settings.setLevelUpChannelId(null);
                    LevelManager.saveGuildData();
                    return message.getChannel().flatMap(channel -> 
                        channel.createMessage("✅ Level-up messages will now be sent to the channel where the user is chatting.")
                    ).then();
                }

                // Simple extraction of channel ID from mention <#ID>
                String content = message.getContent();
                int start = content.indexOf("<#");
                int end = content.indexOf(">", start);
                
                if (start == -1 || end == -1) {
                    return message.getChannel().flatMap(channel -> 
                        channel.createMessage("❌ Please mention a valid channel.")
                    ).then();
                }

                String channelIdStrRaw = content.substring(start + 2, end);
                // Handle cases like <#123456789012345678> or <#&123456789012345678> (though & is for roles)
                // Discord channel mentions are <#ID>
                if (channelIdStrRaw.startsWith("!")) channelIdStrRaw = channelIdStrRaw.substring(1); // Should not happen for channels but good to be safe
                
                final String channelIdStr = channelIdStrRaw;
                settings.setLevelUpChannelId(channelIdStr);
                LevelManager.saveGuildData();

                return message.getChannel().flatMap(channel -> 
                    channel.createMessage("✅ Level-up messages will now be sent to <#" + channelIdStr + ">.")
                ).then();
        });
    }
}
