package onlydust.com.marketplace.api.infrastructure.accounting;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.BillingProfile;
import onlydust.com.marketplace.accounting.domain.port.in.RewardStatusFacadePort;
import onlydust.com.marketplace.accounting.domain.port.out.RewardStatusStorage;
import onlydust.com.marketplace.api.domain.model.OldCompanyBillingProfile;
import onlydust.com.marketplace.api.domain.model.OldIndividualBillingProfile;
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
        refreshRewardsUsdEquivalentOf(BillingProfile.Id.of(event.getBillingProfileId()));
    }

    @Override
    public void onBillingProfilePayoutSettingsUpdated(UUID billingProfileId, UserPayoutSettings payoutSettings) {
        refreshRewardsUsdEquivalentOf(BillingProfile.Id.of(billingProfileId));
    }

    @Override
    public void onBillingProfileSelected(UUID userId, OldIndividualBillingProfile billingProfile) {
        refreshRewardsUsdEquivalentOf(BillingProfile.Id.of(billingProfile.getId()));
    }

    @Override
    public void onBillingProfileSelected(UUID userId, OldCompanyBillingProfile billingProfile) {
        refreshRewardsUsdEquivalentOf(BillingProfile.Id.of(billingProfile.getId()));
    }

    private void refreshRewardsUsdEquivalentOf(BillingProfile.Id billingProfileId) {
        rewardStatusStorage.notPaid(billingProfileId).forEach(rewardStatus ->
                rewardStatusStorage.save(rewardStatus.amountUsdEquivalent(rewardStatusFacadePort.usdEquivalent(rewardStatus.rewardId())))
        );
    }
}
