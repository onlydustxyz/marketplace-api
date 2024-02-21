package onlydust.com.marketplace.project.domain.port.output;

import onlydust.com.marketplace.project.domain.model.notification.BillingProfileUpdated;

public interface NotificationPort {
    void notifyNewVerificationEvent(BillingProfileUpdated billingProfileUpdated);
}
