package onlydust.com.marketplace.kernel.port.output;


import onlydust.com.marketplace.kernel.model.Event;

public interface NotificationPort {
    void notifyNewEvent(Event event);
}
