package onlydust.com.marketplace.accounting.domain.service;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.accounting.domain.model.Network;
import onlydust.com.marketplace.accounting.domain.model.PositiveAmount;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.BillingProfile;
import onlydust.com.marketplace.accounting.domain.model.user.GithubUserId;
import onlydust.com.marketplace.accounting.domain.notification.RewardsPaid;
import onlydust.com.marketplace.accounting.domain.notification.dto.ShortReward;
import onlydust.com.marketplace.accounting.domain.port.in.AccountingFacadePort;
import onlydust.com.marketplace.accounting.domain.port.in.AccountingRewardPort;
import onlydust.com.marketplace.accounting.domain.port.out.AccountingRewardStoragePort;
import onlydust.com.marketplace.accounting.domain.port.out.AccountingSponsorStoragePort;
import onlydust.com.marketplace.accounting.domain.view.EarningsView;
import onlydust.com.marketplace.accounting.domain.view.RewardDetailsView;
import onlydust.com.marketplace.accounting.domain.view.ShortContributorView;
import onlydust.com.marketplace.kernel.model.ProjectId;
import onlydust.com.marketplace.kernel.model.RewardId;
import onlydust.com.marketplace.kernel.model.RewardStatus;
import onlydust.com.marketplace.kernel.port.output.NotificationPort;

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
    private final AccountingFacadePort accountingFacadePort;
    private final AccountingSponsorStoragePort accountingSponsorStoragePort;
    private final NotificationPort notificationPort;

    @Override
    public EarningsView getEarnings(List<RewardStatus.Input> statuses,
                                    List<GithubUserId> recipientIds,
                                    List<BillingProfile.Id> billingProfileIds,
                                    List<ProjectId> projectIds,
                                    Date fromRequestedAt, Date toRequestedAt,
                                    Date fromProcessedAt, Date toProcessedAt) {
        final Set<RewardStatus.Input> sanitizedStatuses = isNull(statuses) ? Set.of() : statuses.stream().collect(Collectors.toUnmodifiableSet());
        return accountingRewardStoragePort.getEarnings(
                sanitizedStatuses,
                isNull(recipientIds) ? List.of() : recipientIds,
                isNull(billingProfileIds) ? List.of() : billingProfileIds,
                isNull(projectIds) ? List.of() : projectIds,
                fromRequestedAt, toRequestedAt,
                fromProcessedAt, toProcessedAt);
    }

    @Override
    public void notifyAllNewPaidRewards() {
        final var rewardViews = accountingRewardStoragePort.findPaidRewardsToNotify();
        for (final var listOfPaidRewardsMapToAdminEmail : rewardViews.stream().collect(groupingBy(rewardView -> rewardView.recipient().email())).entrySet()) {
            final ShortContributorView recipient = listOfPaidRewardsMapToAdminEmail.getValue().get(0).recipient();

            notificationPort.push(recipient.userId(),
                    RewardsPaid.builder()
                            .shortRewards(listOfPaidRewardsMapToAdminEmail.getValue().stream()
                                    .map(rewardDetailsView -> ShortReward.builder().
                                            id(rewardDetailsView.id())
                                            .amount(rewardDetailsView.money().amount())
                                            .projectName(rewardDetailsView.project().name())
                                            .currencyCode(rewardDetailsView.money().currency().code().toString())
                                            .dollarsEquivalent(rewardDetailsView.money().getDollarsEquivalentValue())
                                            .sentByGithubLogin(rewardDetailsView.requester().login())
                                            .contributionsCount(rewardDetailsView.githubUrls().size())
                                            .build()).toList()
                            ).build()
            );
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
                .map(sponsorAccount -> accountingSponsorStoragePort.getView(sponsorAccount.sponsorId()).orElseThrow(() -> notFound("Sponsor %s not found".formatted(id))))
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
