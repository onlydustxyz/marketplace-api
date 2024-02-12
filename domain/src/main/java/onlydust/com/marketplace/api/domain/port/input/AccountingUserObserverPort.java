package onlydust.com.marketplace.api.domain.port.input;

import onlydust.com.marketplace.api.domain.model.CompanyBillingProfile;
import onlydust.com.marketplace.api.domain.model.IndividualBillingProfile;
import onlydust.com.marketplace.api.domain.model.UserPayoutSettings;
import onlydust.com.marketplace.api.domain.model.notification.BillingProfileUpdated;

import java.util.UUID;

public interface AccountingUserObserverPort {
    void onBillingProfileUpdated(BillingProfileUpdated event);

    void onBillingProfilePayoutSettingsUpdated(UUID billingProfileId, UserPayoutSettings payoutSettings);

    void onBillingProfileSelected(UUID userId, IndividualBillingProfile billingProfile);

    void onBillingProfileSelected(UUID userId, CompanyBillingProfile billingProfile);
}
