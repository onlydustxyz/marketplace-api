package onlydust.com.marketplace.api.infrastructure.accounting;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.domain.model.CompanyBillingProfile;
import onlydust.com.marketplace.api.domain.model.IndividualBillingProfile;
import onlydust.com.marketplace.api.domain.model.User;
import onlydust.com.marketplace.api.domain.model.UserPayoutSettings;
import onlydust.com.marketplace.api.domain.model.notification.BillingProfileUpdated;
import onlydust.com.marketplace.api.domain.port.input.AccountingUserObserverPort;

import java.util.UUID;

@AllArgsConstructor
public class AccountingObserverAdapter implements AccountingUserObserverPort {
    @Override
    public void onBillingProfileUpdated(BillingProfileUpdated event) {

    }

    @Override
    public void onBillingProfilePayoutSettingsUpdated(UUID billingProfileId, UserPayoutSettings payoutSettings) {

    }

    @Override
    public void onBillingProfileSelected(UUID projectId, User user, IndividualBillingProfile billingProfile) {

    }

    @Override
    public void onBillingProfileSelected(UUID projectId, User user, CompanyBillingProfile billingProfile) {

    }
}
