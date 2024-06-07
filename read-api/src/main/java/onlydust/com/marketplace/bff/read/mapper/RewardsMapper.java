package onlydust.com.marketplace.bff.read.mapper;

import onlydust.com.marketplace.api.contract.model.*;
import onlydust.com.marketplace.api.rest.api.adapter.mapper.DateMapper;
import onlydust.com.marketplace.api.rest.api.adapter.mapper.MoneyMapper;
import onlydust.com.marketplace.bff.read.entities.project.BudgetStatsReadEntity;
import onlydust.com.marketplace.bff.read.entities.reward.RewardDetailsReadEntity;
import onlydust.com.marketplace.bff.read.entities.user.UserRewardStatsReadEntity;
import onlydust.com.marketplace.kernel.model.AuthenticatedUser;
import onlydust.com.marketplace.kernel.pagination.PaginationHelper;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static onlydust.com.marketplace.kernel.mapper.AmountMapper.prettyUsd;

public interface RewardsMapper {

    static RewardsPageResponse mapProjectRewardPageToResponse(final Integer pageIndex,
                                                              final Page<RewardDetailsReadEntity> page,
                                                              final List<BudgetStatsReadEntity> budgetStatsReadEntities,
                                                              final AuthenticatedUser authenticatedUser) {


        final RewardsPageResponse rewardsPageResponse = new RewardsPageResponse()
                .hasMore(PaginationHelper.hasMore(pageIndex, page.getTotalPages()))
                .totalPageNumber(page.getTotalPages())
                .totalItemNumber((int) page.getTotalElements())
                .nextPageIndex(PaginationHelper.nextPageIndex(pageIndex, page.getTotalPages()))
                .remainingBudget(new DetailedTotalMoney()
                        .totalUsdEquivalent(totalRemainingUsdEquivalent(budgetStatsReadEntities))
                        .totalPerCurrency(budgetStatsReadEntities.stream()
                                .map(BudgetStatsReadEntity::toRemainingMoney)
                                .map(MoneyMapper::toMoney)
                                .toList()))
                .spentAmount(new DetailedTotalMoney()
                        .totalUsdEquivalent(totalSpentUsdEquivalent(budgetStatsReadEntities))
                        .totalPerCurrency(budgetStatsReadEntities.stream()
                                .map(BudgetStatsReadEntity::toSpentMoney)
                                .map(MoneyMapper::toMoney)
                                .toList()))
                .sentRewardsCount(budgetStatsReadEntities.stream()
                        .map(BudgetStatsReadEntity::getRewardIds).flatMap(Collection::stream).collect(Collectors.toUnmodifiableSet()).size())
                .rewardedContributionsCount(budgetStatsReadEntities.stream()
                        .map(BudgetStatsReadEntity::getRewardItemIds).flatMap(Collection::stream)
                        .flatMap(Collection::stream).collect(Collectors.toUnmodifiableSet()).size())
                .rewardedContributorsCount(budgetStatsReadEntities.stream()
                        .map(BudgetStatsReadEntity::getRewardRecipientIds).flatMap(Collection::stream).collect(Collectors.toUnmodifiableSet()).size());

        page.getContent().stream()
                .map(r -> mapProjectRewardToResponse(r, authenticatedUser))
                .forEach(rewardsPageResponse::addRewardsItem);

        return rewardsPageResponse;
    }

    static RewardPageItemResponse mapProjectRewardToResponse(final RewardDetailsReadEntity reward, AuthenticatedUser authenticatedUser) {
        final RewardPageItemResponse rewardPageItemResponse = new RewardPageItemResponse();
        rewardPageItemResponse.setId(reward.getId());
        rewardPageItemResponse.setNumberOfRewardedContributions(reward.getContributionCount());
        rewardPageItemResponse.setRewardedUser(reward.toContributorResponse());
        rewardPageItemResponse.setAmount(reward.amount());
        rewardPageItemResponse.setStatus(reward.statusAsUser(authenticatedUser));
        rewardPageItemResponse.setRequestedAt(DateMapper.toZoneDateTime(reward.getRequestedAt()));
        rewardPageItemResponse.setProcessedAt(DateMapper.toZoneDateTime(reward.getStatusData().paidAt()));
        rewardPageItemResponse.setUnlockDate(DateMapper.toZoneDateTime(reward.getStatusData().unlockDate()));
        return rewardPageItemResponse;
    }

    static BigDecimal totalRemainingUsdEquivalent(final List<BudgetStatsReadEntity> budgetStatsReadEntities) {
        return prettyUsd(budgetStatsReadEntities.stream()
                .map(BudgetStatsReadEntity::getRemainingUsdAmount)
                .map(amount -> Optional.ofNullable(amount).orElse(BigDecimal.ZERO))
                .reduce(BigDecimal.ZERO, BigDecimal::add));
    }

    static BigDecimal totalSpentUsdEquivalent(final List<BudgetStatsReadEntity> budgetStatsReadEntities) {
        return prettyUsd(budgetStatsReadEntities.stream()
                .map(BudgetStatsReadEntity::getSpentUsdAmount)
                .map(amount -> Optional.ofNullable(amount).orElse(BigDecimal.ZERO))
                .reduce(BigDecimal.ZERO, BigDecimal::add));
    }


    static MyRewardsPageResponse mapMyRewardsToResponse(final int pageIndex,
                                                        final Page<RewardDetailsReadEntity> page,
                                                        final List<UserRewardStatsReadEntity> userRewardStatsReadEntities,
                                                        final AuthenticatedUser authenticatedUser) {
        return new MyRewardsPageResponse()
                .hasMore(PaginationHelper.hasMore(pageIndex, page.getTotalPages()))
                .totalPageNumber(page.getTotalPages())
                .totalItemNumber((int) page.getTotalElements())
                .nextPageIndex(PaginationHelper.nextPageIndex(pageIndex, page.getTotalPages()))
                .rewards(page.getContent().stream().map((RewardDetailsReadEntity readEntity) -> mapMyRewardToResponse(readEntity, authenticatedUser)).toList())
                .pendingAmount(new DetailedTotalMoney()
                        .totalUsdEquivalent(totalPendingAmountUsdEquivalent(userRewardStatsReadEntities))
                        .totalPerCurrency(userRewardStatsReadEntities.stream()
                                .map(UserRewardStatsReadEntity::toPendingMoney)
                                .map(MoneyMapper::toMoney)
                                .toList()))
                .rewardedAmount(new DetailedTotalMoney()
                        .totalUsdEquivalent(totalRewardedAmountUsdEquivalent(userRewardStatsReadEntities))
                        .totalPerCurrency(userRewardStatsReadEntities.stream()
                                .map(UserRewardStatsReadEntity::toRewardedMoney)
                                .map(MoneyMapper::toMoney)
                                .toList()))
                .receivedRewardsCount(userRewardStatsReadEntities.stream().map(UserRewardStatsReadEntity::getRewardIds).flatMap(Collection::stream).collect(Collectors.toUnmodifiableSet()).size())
                .rewardedContributionsCount(userRewardStatsReadEntities.stream().map(UserRewardStatsReadEntity::getRewardItemIds).flatMap(Collection::stream).flatMap(Collection::stream).collect(Collectors.toUnmodifiableSet()).size())
                .rewardingProjectsCount(userRewardStatsReadEntities.stream().map(UserRewardStatsReadEntity::getProjectIds).flatMap(Collection::stream).collect(Collectors.toUnmodifiableSet()).size())
                .pendingRequestCount(userRewardStatsReadEntities.size() == 1 ? userRewardStatsReadEntities.get(0).getPendingRequestCount() :
                        userRewardStatsReadEntities.stream().map(UserRewardStatsReadEntity::getPendingRequestCount).filter(Objects::nonNull).reduce(0,
                                Integer::sum))
                ;
    }

    static BigDecimal totalRewardedAmountUsdEquivalent(final List<UserRewardStatsReadEntity> userRewardStatsReadEntities) {
        return prettyUsd(userRewardStatsReadEntities.stream()
                .map(UserRewardStatsReadEntity::getProcessedUsdAmount)
                .map(money -> Optional.ofNullable(money).orElse(BigDecimal.ZERO))
                .reduce(BigDecimal.ZERO, BigDecimal::add));
    }

    static BigDecimal totalPendingAmountUsdEquivalent(final List<UserRewardStatsReadEntity> userRewardStatsReadEntities) {
        return prettyUsd(userRewardStatsReadEntities.stream()
                .map(UserRewardStatsReadEntity::getPendingUsdAmount)
                .map(money -> Optional.ofNullable(money).orElse(BigDecimal.ZERO))
                .reduce(BigDecimal.ZERO, BigDecimal::add));
    }

    static MyRewardPageItemResponse mapMyRewardToResponse(final RewardDetailsReadEntity readEntity, AuthenticatedUser authenticatedUser) {
        return new MyRewardPageItemResponse()
                .id(readEntity.getId())
                .projectId(readEntity.getProject().getId())
                .numberOfRewardedContributions(readEntity.getContributionCount())
                .rewardedOnProjectLogoUrl(readEntity.getProject().getLogoUrl())
                .rewardedOnProjectName(readEntity.getProject().getName())
                .amount(readEntity.amount())
                .rewardedUser(readEntity.toContributorResponse())
                .status(readEntity.statusAsUser(authenticatedUser))
                .requestedAt(DateMapper.toZoneDateTime(readEntity.getRequestedAt()))
                .processedAt(DateMapper.toZoneDateTime(readEntity.getStatusData().paidAt()))
                .unlockDate(DateMapper.toZoneDateTime(readEntity.getStatusData().unlockDate()))
                .billingProfileId(readEntity.getBillingProfileId())
                ;
    }
}
