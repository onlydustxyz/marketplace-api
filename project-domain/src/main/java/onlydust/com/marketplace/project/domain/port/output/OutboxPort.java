package onlydust.com.marketplace.project.domain.port.output;

import onlydust.com.marketplace.project.domain.model.notification.Event;

import java.util.Optional;

public interface OutboxPort {
    void push(Event event);

    Optional<Event> peek();

    void ack();

    void nack(String message);

    void skip(String someReasonToSkip);
}
