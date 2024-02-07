package onlydust.com.marketplace.api.domain.port.input;

import onlydust.com.marketplace.api.domain.model.CompanyBillingProfile;
import onlydust.com.marketplace.api.domain.model.IndividualBillingProfile;
import onlydust.com.marketplace.api.domain.model.User;
import onlydust.com.marketplace.api.domain.model.UserPayoutInformation;
import onlydust.com.marketplace.api.domain.model.notification.BillingProfileUpdated;

import java.util.UUID;

public interface AccountingUserObserverPort {
    void onBillingProfileUpdated(BillingProfileUpdated event);

    void onBillingProfilePayoutSettingsUpdated(UUID billingProfileId, UserPayoutInformation.PayoutSettings payoutSettings);

    void onBillingProfileSelected(UUID projectId, User user, IndividualBillingProfile billingProfile);

    void onBillingProfileSelected(UUID projectId, User user, CompanyBillingProfile billingProfile);
}
