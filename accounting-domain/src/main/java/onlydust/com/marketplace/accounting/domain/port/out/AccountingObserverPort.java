package onlydust.com.marketplace.accounting.domain.port.out;

import onlydust.com.marketplace.accounting.domain.model.Payment;
import onlydust.com.marketplace.accounting.domain.model.ProjectId;
import onlydust.com.marketplace.accounting.domain.model.RewardId;
import onlydust.com.marketplace.accounting.domain.model.SponsorAccountStatement;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.BillingProfile;
import onlydust.com.marketplace.accounting.domain.model.user.UserId;
import onlydust.com.marketplace.accounting.domain.service.AccountBookFacade;

public interface AccountingObserverPort {
    void onSponsorAccountBalanceChanged(SponsorAccountStatement sponsorAccount);

    void onSponsorAccountUpdated(SponsorAccountStatement sponsorAccount);

    void onRewardCreated(RewardId rewardId, AccountBookFacade accountBookFacade);

    void onRewardCancelled(RewardId rewardId);

    void onRewardPaid(RewardId rewardId);

    void onPaymentReceived(RewardId rewardId, Payment.Reference reference);

    void onPayoutPreferenceChanged(BillingProfile.Id billingProfileId, UserId userId, ProjectId projectId);

    void onBillingProfileEnableChanged(BillingProfile.Id billingProfileId, Boolean enabled);

    void onBillingProfileDeleted(BillingProfile.Id billingProfileId);

}
