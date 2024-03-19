package onlydust.com.marketplace.accounting.domain.port.out;

import onlydust.com.marketplace.accounting.domain.model.RewardId;
import onlydust.com.marketplace.accounting.domain.model.SponsorAccount;
import onlydust.com.marketplace.accounting.domain.model.SponsorAccountStatement;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.BillingProfile;
import onlydust.com.marketplace.accounting.domain.service.AccountBookFacade;

public interface AccountingObserverPort {
    void onSponsorAccountBalanceChanged(SponsorAccountStatement sponsorAccount);

    void onSponsorAccountUpdated(SponsorAccountStatement sponsorAccount);

    void onRewardCreated(RewardId rewardId, AccountBookFacade accountBookFacade);

    void onRewardCancelled(RewardId rewardId);

    void onRewardPaid(RewardId rewardId);

    void onPaymentReceived(RewardId rewardId, SponsorAccount.PaymentReference reference);

    void onPayoutPreferenceChanged(BillingProfile.Id billingProfileId);
}
