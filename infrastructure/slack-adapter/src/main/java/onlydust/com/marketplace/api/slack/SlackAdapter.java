package onlydust.com.marketplace.api.slack;

import onlydust.com.marketplace.api.domain.model.notification.BillingProfileUpdated;
import onlydust.com.marketplace.api.domain.port.output.NotificationPort;

public class SlackAdapter implements NotificationPort {

    @Override
    public void notifyNewVerificationEvent(BillingProfileUpdated billingProfileUpdated) {

    }
}
