package onlydust.com.marketplace.kernel.port.output;

import onlydust.com.marketplace.kernel.model.Event;

import java.util.List;

public interface OutboxPort {
    void push(Event event);

    List<IdentifiableEvent> peek();

    void ack(Long eventId);

    void nack(Long eventId, String message);

    void skip(Long eventId, String someReasonToSkip);

    record IdentifiableEvent(Long id, Event event) {
    }
}
