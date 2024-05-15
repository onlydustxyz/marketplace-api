package onlydust.com.marketplace.kernel.service;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.kernel.model.Event;
import onlydust.com.marketplace.kernel.port.output.NotificationPort;
import onlydust.com.marketplace.kernel.port.output.OutboxPort;

@AllArgsConstructor
public class OutboxNotifier implements NotificationPort {

    private final OutboxPort outboxPort;

    @Override
    public void notify(Event event) {
        outboxPort.push(event);
    }
}
