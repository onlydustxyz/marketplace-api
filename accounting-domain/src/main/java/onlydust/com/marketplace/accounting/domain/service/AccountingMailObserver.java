package onlydust.com.marketplace.accounting.domain.service;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.kernel.model.Event;
import onlydust.com.marketplace.kernel.observer.MailObserver;
import onlydust.com.marketplace.kernel.port.output.OutboxPort;

@AllArgsConstructor
public class AccountingMailObserver implements MailObserver {

    private final OutboxPort accountingMailOutbox;

    @Override
    public void send(Event event) {
        accountingMailOutbox.push(event);
    }
}
