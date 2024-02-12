package onlydust.com.marketplace.api.infrastructure.accounting;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.accounting.domain.model.BillingProfileId;
import onlydust.com.marketplace.accounting.domain.port.in.RewardStatusFacadePort;
import onlydust.com.marketplace.accounting.domain.port.out.RewardStatusStorage;
import onlydust.com.marketplace.api.domain.model.CompanyBillingProfile;
import onlydust.com.marketplace.api.domain.model.IndividualBillingProfile;
import onlydust.com.marketplace.api.domain.model.UserPayoutSettings;
import onlydust.com.marketplace.api.domain.model.notification.BillingProfileUpdated;
import onlydust.com.marketplace.api.domain.port.input.AccountingUserObserverPort;

import java.util.UUID;

@AllArgsConstructor
public class AccountingObserverAdapter implements AccountingUserObserverPort {
    final RewardStatusStorage rewardStatusStorage;
    final RewardStatusFacadePort rewardStatusFacadePort;

    final
    @Override
    public void onBillingProfileUpdated(BillingProfileUpdated event) {
        refreshRewardsUsdEquivalentOf(BillingProfileId.of(event.getBillingProfileId()));
    }

    @Override
    public void onBillingProfilePayoutSettingsUpdated(UUID billingProfileId, UserPayoutSettings payoutSettings) {
        refreshRewardsUsdEquivalentOf(BillingProfileId.of(billingProfileId));
    }

    @Override
    public void onBillingProfileSelected(UUID userId, IndividualBillingProfile billingProfile) {
        refreshRewardsUsdEquivalentOf(BillingProfileId.of(billingProfile.getId()));
    }

    @Override
    public void onBillingProfileSelected(UUID userId, CompanyBillingProfile billingProfile) {
        refreshRewardsUsdEquivalentOf(BillingProfileId.of(billingProfile.getId()));
    }

    private void refreshRewardsUsdEquivalentOf(BillingProfileId billingProfileId) {
        rewardStatusStorage.notPaid(billingProfileId).forEach(rewardStatus ->
                rewardStatusStorage.save(rewardStatus.amountUsdEquivalent(rewardStatusFacadePort.usdEquivalent(rewardStatus.rewardId())))
        );
    }
}
