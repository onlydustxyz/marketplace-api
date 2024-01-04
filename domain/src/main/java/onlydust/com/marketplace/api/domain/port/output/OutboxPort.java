package onlydust.com.marketplace.api.domain.port.output;

import java.util.Optional;
import onlydust.com.marketplace.api.domain.model.notification.Event;

public interface OutboxPort {

  void push(Event event);

  Optional<Event> peek();

  void ack();

  void nack(String message);
}
