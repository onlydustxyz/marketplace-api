package onlydust.com.marketplace.project.domain.job;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import onlydust.com.marketplace.kernel.model.Event;
import onlydust.com.marketplace.kernel.port.output.OutboxConsumer;
import onlydust.com.marketplace.project.domain.port.output.TrackingEventPublisher;

@Slf4j
@AllArgsConstructor
public class TrackingEventPublisherOutboxConsumer implements OutboxConsumer {
    private final TrackingEventPublisher trackingEventPublisher;

    @Override
    public void process(Event event) {
        trackingEventPublisher.publish(event);
    }
}
