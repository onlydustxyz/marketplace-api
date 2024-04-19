package onlydust.com.marketplace.kernel.observer;

import onlydust.com.marketplace.kernel.model.Event;

public interface MailObserver {

    void send(Event event);
}
