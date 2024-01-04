package onlydust.com.marketplace.api.domain.job;

import onlydust.com.marketplace.api.domain.model.notification.Event;

public interface OutboxConsumer {

  void process(Event event);
}
