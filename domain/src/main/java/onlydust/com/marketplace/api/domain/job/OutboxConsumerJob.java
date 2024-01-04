package onlydust.com.marketplace.api.domain.job;

import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import onlydust.com.marketplace.api.domain.model.notification.Event;
import onlydust.com.marketplace.api.domain.port.output.OutboxPort;

@Slf4j
@AllArgsConstructor
public class OutboxConsumerJob implements Runnable {

  private final OutboxPort outbox;
  private final OutboxConsumer consumer;

  @Override
  public void run() {
    try {
      Optional<Event> event;
      while ((event = outbox.peek()).isPresent()) {
        consumer.process(event.get());
        outbox.ack();
      }
    } catch (Exception e) {
      LOGGER.error("Error while processing event", e);
      outbox.nack(e.getMessage());
    }
  }
}
