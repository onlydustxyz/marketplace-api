package onlydust.com.marketplace.accounting.domain.service;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.accounting.domain.model.RewardId;
import onlydust.com.marketplace.accounting.domain.model.RewardStatus;
import onlydust.com.marketplace.accounting.domain.model.SponsorAccountStatement;
import onlydust.com.marketplace.accounting.domain.port.out.AccountingObserver;
import onlydust.com.marketplace.accounting.domain.port.out.RewardStatusStorage;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;

@AllArgsConstructor
public class RewardStatusService implements AccountingObserver {
    private final RewardStatusStorage rewardStatusStorage;

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
            rewardStatusStorage.save(uptodateRewardStatus(sponsorAccount.accountBookFacade(), rewardStatus));
        });
    }

    @Override
    public void onRewardCreated(RewardId rewardId, AccountBookFacade accountBookFacade) {
        rewardStatusStorage.save(uptodateRewardStatus(accountBookFacade, new RewardStatus(rewardId)));
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

    private RewardStatus uptodateRewardStatus(AccountBookFacade accountBookFacade, RewardStatus rewardStatus) {
        return rewardStatus
                .sponsorHasEnoughFund(accountBookFacade.isFunded(rewardStatus.rewardId()))
                .unlockDate(accountBookFacade.unlockDateOf(rewardStatus.rewardId()).map(d -> d.atZone(ZoneOffset.UTC)).orElse(null))
                .withAdditionalNetworks(accountBookFacade.networksOf(rewardStatus.rewardId()));
    }
}
