package onlydust.com.marketplace.accounting.domain.service;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.accounting.domain.model.RewardId;
import onlydust.com.marketplace.accounting.domain.model.RewardStatus;
import onlydust.com.marketplace.accounting.domain.model.SponsorAccountStatement;
import onlydust.com.marketplace.accounting.domain.port.in.AccountingFacadePort;
import onlydust.com.marketplace.accounting.domain.port.out.AccountingObserver;
import onlydust.com.marketplace.accounting.domain.port.out.RewardStatusStorage;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;

import java.time.ZonedDateTime;

@AllArgsConstructor
public class RewardStatusService implements AccountingObserver {
    private final RewardStatusStorage rewardStatusStorage;
    private final AccountingFacadePort accountingFacadePort;

    @Override
    public void onSponsorAccountBalanceChanged(SponsorAccountStatement sponsorAccount) {
        refreshRelatedRewardsStatuses(sponsorAccount);
    }

    @Override
    public void onSponsorAccountUpdated(SponsorAccountStatement sponsorAccount) {
        refreshRelatedRewardsStatuses(sponsorAccount);
    }

    private void refreshRelatedRewardsStatuses(SponsorAccountStatement sponsorAccount) {
        sponsorAccount.awaitingPayments().forEach((rewardId, amount) -> {
            final var rewardStatus = rewardStatusStorage.get(rewardId)
                    .orElseThrow(() -> OnlyDustException.notFound("RewardStatus not found for reward %s".formatted(rewardId)));
            rewardStatusStorage.save(accountingFacadePort.uptodateRewardStatus(sponsorAccount.accountBookState(), rewardStatus));
        });
    }

    @Override
    public void onRewardCreated(RewardStatus rewardStatus) {
        rewardStatusStorage.save(rewardStatus);
    }

    @Override
    public void onRewardCancelled(RewardId rewardId) {
        rewardStatusStorage.delete(rewardId);
    }

    @Override
    public void onRewardPaid(RewardId rewardId) {
        final var rewardStatus = rewardStatusStorage.get(rewardId)
                .orElseThrow(() -> OnlyDustException.notFound("RewardStatus not found for reward %s".formatted(rewardId)));
        rewardStatusStorage.save(rewardStatus.paidAt(ZonedDateTime.now()));
    }
}
