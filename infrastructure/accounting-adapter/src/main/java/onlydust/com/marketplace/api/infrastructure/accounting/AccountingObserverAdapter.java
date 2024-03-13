package onlydust.com.marketplace.api.infrastructure.accounting;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.BillingProfile;
import onlydust.com.marketplace.accounting.domain.port.in.RewardStatusFacadePort;
import onlydust.com.marketplace.accounting.domain.port.out.RewardStatusStorage;
import onlydust.com.marketplace.project.domain.model.UserPayoutSettings;
import onlydust.com.marketplace.project.domain.port.input.AccountingUserObserverPort;

import java.util.UUID;

@AllArgsConstructor
public class AccountingObserverAdapter implements AccountingUserObserverPort {
    final RewardStatusStorage rewardStatusStorage;
    final RewardStatusFacadePort rewardStatusFacadePort;

    @Override
    public void onBillingProfilePayoutSettingsUpdated(UUID billingProfileId, UserPayoutSettings payoutSettings) {
        refreshRewardsUsdEquivalentOf(BillingProfile.Id.of(billingProfileId));
    }

    private void refreshRewardsUsdEquivalentOf(BillingProfile.Id billingProfileId) {
        rewardStatusStorage.notPaid(billingProfileId).forEach(rewardStatus ->
                rewardStatusStorage.save(rewardStatus.usdAmount(rewardStatusFacadePort.usdAmountOf(rewardStatus.rewardId()).orElse(null)))
        );
    }
}
