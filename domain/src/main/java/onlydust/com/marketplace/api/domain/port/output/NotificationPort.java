package onlydust.com.marketplace.api.domain.port.output;

import onlydust.com.marketplace.api.domain.model.notification.BillingProfileUpdated;

public interface NotificationPort {
    void notifyNewVerificationEvent(BillingProfileUpdated billingProfileUpdated);
}
