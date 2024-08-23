package onlydust.com.marketplace.accounting.domain.service;

import onlydust.com.marketplace.kernel.model.ProjectId;
import onlydust.com.marketplace.kernel.model.RewardId;
import onlydust.com.marketplace.accounting.domain.model.SponsorAccountStatement;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.BillingProfile;
import onlydust.com.marketplace.kernel.model.UserId;
import onlydust.com.marketplace.accounting.domain.port.out.AccountingObserverPort;

import java.util.List;

public class AccountingObserverComposite implements AccountingObserverPort {
    private final List<AccountingObserverPort> observers;

    public AccountingObserverComposite(AccountingObserverPort... observers) {
        this.observers = List.of(observers);
    }

    @Override
    public void onSponsorAccountBalanceChanged(SponsorAccountStatement sponsorAccount) {
        observers.forEach(o -> o.onSponsorAccountBalanceChanged(sponsorAccount));
    }

    @Override
    public void onSponsorAccountUpdated(SponsorAccountStatement sponsorAccount) {
        observers.forEach(o -> o.onSponsorAccountUpdated(sponsorAccount));
    }

    @Override
    public void onRewardCreated(RewardId rewardId, AccountBookFacade accountBookFacade) {
        observers.forEach(o -> o.onRewardCreated(rewardId, accountBookFacade));
    }

    @Override
    public void onRewardCancelled(RewardId rewardId) {
        observers.forEach(o -> o.onRewardCancelled(rewardId));
    }

    @Override
    public void onRewardPaid(RewardId rewardId) {
        observers.forEach(o -> o.onRewardPaid(rewardId));
    }

    @Override
    public void onPayoutPreferenceChanged(BillingProfile.Id billingProfileId, UserId userId, ProjectId projectId) {
        observers.forEach(o -> o.onPayoutPreferenceChanged(billingProfileId, userId, projectId));
    }

    @Override
    public void onBillingProfileEnableChanged(BillingProfile.Id billingProfileId, Boolean enabled) {
        observers.forEach(o -> o.onBillingProfileEnableChanged(billingProfileId, enabled));
    }

    @Override
    public void onBillingProfileDeleted(BillingProfile.Id billingProfileId) {
        observers.forEach(o -> o.onBillingProfileDeleted(billingProfileId));
    }
}
