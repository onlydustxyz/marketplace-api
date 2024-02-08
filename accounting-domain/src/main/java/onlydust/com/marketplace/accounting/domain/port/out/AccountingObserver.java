package onlydust.com.marketplace.accounting.domain.port.out;

import onlydust.com.marketplace.accounting.domain.model.RewardId;
import onlydust.com.marketplace.accounting.domain.model.RewardStatus;
import onlydust.com.marketplace.accounting.domain.model.SponsorAccountStatement;

public interface AccountingObserver {
    void onSponsorAccountBalanceChanged(SponsorAccountStatement sponsorAccount);

    void onRewardCreated(RewardStatus rewardStatus);

    void onRewardCancelled(RewardId rewardId);
}
