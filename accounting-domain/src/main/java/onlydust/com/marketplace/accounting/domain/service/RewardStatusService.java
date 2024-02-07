package onlydust.com.marketplace.accounting.domain.service;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.accounting.domain.model.RewardStatus;
import onlydust.com.marketplace.accounting.domain.model.SponsorAccount;
import onlydust.com.marketplace.accounting.domain.model.accountbook.AccountBookAggregate;
import onlydust.com.marketplace.accounting.domain.port.out.AccountingObserver;
import onlydust.com.marketplace.accounting.domain.port.out.RewardStatusStorage;

@AllArgsConstructor
public class RewardStatusService implements AccountingObserver {
    private final RewardStatusStorage rewardStatusStorage;

    @Override
    public void onSponsorAccountBalanceChanged(SponsorAccount sponsorAccount, AccountBookAggregate accountBook) {
    }

    @Override
    public void onRewardCreated(RewardStatus rewardStatus) {
        rewardStatusStorage.save(rewardStatus);
    }
}
