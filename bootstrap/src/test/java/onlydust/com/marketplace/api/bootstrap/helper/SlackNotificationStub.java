package onlydust.com.marketplace.api.bootstrap.helper;

import lombok.Getter;
import onlydust.com.marketplace.accounting.domain.events.BillingProfileVerificationUpdated;
import onlydust.com.marketplace.kernel.model.Event;
import onlydust.com.marketplace.kernel.port.output.NotificationPort;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Getter
public class SlackNotificationStub implements NotificationPort {

    private final List<BillingProfileVerificationUpdated> notifications = Collections.synchronizedList(new ArrayList<>());

    @Override
    public void notifyNewEvent(Event event) {
        if (event instanceof BillingProfileVerificationUpdated billingProfileVerificationUpdated) {
            this.notifications.add(billingProfileVerificationUpdated);
        }
    }
}
