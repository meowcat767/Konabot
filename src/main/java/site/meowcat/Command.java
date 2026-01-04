package site.meowcat;
import discord4j.core.event.domain.message.MessageCreateEvent;
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
}
