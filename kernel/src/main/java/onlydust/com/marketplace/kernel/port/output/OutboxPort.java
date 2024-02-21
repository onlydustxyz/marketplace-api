package onlydust.com.marketplace.kernel.port.output;

import onlydust.com.marketplace.kernel.model.Event;

import java.util.Optional;

public interface OutboxPort {
    void push(Event event);

    Optional<Event> peek();

    void ack();

    void nack(String message);

    void skip(String someReasonToSkip);
}
