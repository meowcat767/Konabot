package site.meowcat.commands;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.rest.util.Permission;
import reactor.core.publisher.Mono;

public interface Command {
    /**
     * The trigger text for this command, e.g !konata
     */
    String getTrigger();

    /**
     * Called when a message matches the trigger
     */
    Mono<Void> execute(MessageCreateEvent event);

    /**
     * The default permission required to run this command.
     */
    default Permission getDefaultPermission() {
        return null;
    }
}
