package onlydust.com.marketplace.project.domain.port.input;

import onlydust.com.marketplace.project.domain.model.OldCompanyBillingProfile;
import onlydust.com.marketplace.project.domain.model.OldIndividualBillingProfile;
import onlydust.com.marketplace.project.domain.model.UserPayoutSettings;

import java.util.UUID;

public interface AccountingUserObserverPort {

    void onBillingProfilePayoutSettingsUpdated(UUID billingProfileId, UserPayoutSettings payoutSettings);

    void onBillingProfileSelected(UUID userId, OldIndividualBillingProfile billingProfile);

    void onBillingProfileSelected(UUID userId, OldCompanyBillingProfile billingProfile);
}
