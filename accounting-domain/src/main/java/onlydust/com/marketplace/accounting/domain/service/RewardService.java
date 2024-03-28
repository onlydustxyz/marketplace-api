package onlydust.com.marketplace.accounting.domain.service;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.accounting.domain.model.Network;
import onlydust.com.marketplace.accounting.domain.model.PositiveAmount;
import onlydust.com.marketplace.accounting.domain.model.RewardId;
import onlydust.com.marketplace.accounting.domain.port.in.AccountingFacadePort;
import onlydust.com.marketplace.accounting.domain.port.in.AccountingRewardPort;
import onlydust.com.marketplace.accounting.domain.port.out.AccountingRewardStoragePort;
import onlydust.com.marketplace.accounting.domain.port.out.MailNotificationPort;
import onlydust.com.marketplace.accounting.domain.port.out.SponsorStoragePort;
import onlydust.com.marketplace.accounting.domain.view.RewardDetailsView;
import onlydust.com.marketplace.accounting.domain.view.SponsorView;
import onlydust.com.marketplace.kernel.model.RewardStatus;
import onlydust.com.marketplace.kernel.pagination.Page;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.reducing;
import static onlydust.com.marketplace.kernel.exception.OnlyDustException.badRequest;
import static onlydust.com.marketplace.kernel.exception.OnlyDustException.notFound;

@AllArgsConstructor
public class RewardService implements AccountingRewardPort {
    private final AccountingRewardStoragePort accountingRewardStoragePort;
    private final MailNotificationPort mailNotificationPort;
    private final AccountingFacadePort accountingFacadePort;
    private final SponsorStoragePort sponsorStoragePort;

    @Override
    public Page<RewardDetailsView> getRewards(int pageIndex, int pageSize,
                                              List<RewardStatus> statuses,
                                              Date fromRequestedAt, Date toRequestedAt,
                                              Date fromProcessedAt, Date toProcessedAt) {
        final Set<RewardStatus> sanitizedStatuses = isNull(statuses) ? Set.of() : statuses.stream().collect(Collectors.toUnmodifiableSet());
        return accountingRewardStoragePort.findRewards(pageIndex, pageSize, sanitizedStatuses, fromRequestedAt, toRequestedAt, fromProcessedAt, toProcessedAt);
    }

    @Override
    public String exportRewardsCSV(List<RewardStatus> statuses,
                                   Date fromRequestedAt, Date toRequestedAt,
                                   Date fromProcessedAt, Date toProcessedAt) {
        final var rewards = accountingRewardStoragePort.findRewards(0, 1_000_000,
                statuses.stream().collect(Collectors.toUnmodifiableSet()), fromRequestedAt, toRequestedAt, fromProcessedAt, toProcessedAt);

        if (rewards.getTotalPageNumber() > 1) {
            throw badRequest("Too many rewards to export");
        }

        return RewardsExporter.csv(rewards.getContent());
    }

    @Override
    public void notifyAllNewPaidRewards() {
        final var rewardViews = accountingRewardStoragePort.findPaidRewardsToNotify();
        for (final var listOfPaidRewardsMapToAdminEmail :
                rewardViews.stream().collect(groupingBy(rewardView -> rewardView.invoice().createdBy().email())).entrySet()) {
            mailNotificationPort.sendRewardsPaidMail(listOfPaidRewardsMapToAdminEmail.getKey(), listOfPaidRewardsMapToAdminEmail.getValue());
        }
        accountingRewardStoragePort.markRewardsAsPaymentNotified(rewardViews.stream()
                .map(RewardDetailsView::id)
                .toList());
    }

    @Override
    public RewardDetailsView getReward(RewardId id) {
        final var reward = accountingRewardStoragePort.getReward(id)
                .orElseThrow(() -> badRequest("Reward %s not found".formatted(id)));

        final var sponsors = accountingFacadePort.transferredAmountPerOrigin(reward.id(), reward.money().currency().id()).keySet().stream()
                .map(sponsorAccount -> sponsorStoragePort.get(sponsorAccount.sponsorId()).orElseThrow(() -> notFound("Sponsor %s not found".formatted(id))))
                .map(SponsorView::toShortView)
                .toList();

        final Map<Network, PositiveAmount> pendingPayments = reward.invoice() == null ? new HashMap<>() :
                accountingFacadePort.balancesPerOrigin(reward.id(), reward.money().currency().id())
                        .entrySet().stream()
                        .filter(e -> e.getValue().isStrictlyPositive())
                        .collect(groupingBy(e -> e.getKey().network().orElseThrow(), reducing(PositiveAmount.ZERO, Map.Entry::getValue, PositiveAmount::add)));

        return reward.toBuilder()
                .sponsors(sponsors)
                .pendingPayments(pendingPayments)
                .build();
    }
}
