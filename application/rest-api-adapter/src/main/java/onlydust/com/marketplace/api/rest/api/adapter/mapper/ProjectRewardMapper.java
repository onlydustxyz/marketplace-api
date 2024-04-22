package onlydust.com.marketplace.api.rest.api.adapter.mapper;

import onlydust.com.marketplace.api.contract.model.DetailedTotalMoney;
import onlydust.com.marketplace.api.contract.model.RewardPageItemResponse;
import onlydust.com.marketplace.api.contract.model.RewardsPageResponse;
import onlydust.com.marketplace.kernel.model.AuthenticatedUser;
import onlydust.com.marketplace.kernel.pagination.PaginationHelper;
import onlydust.com.marketplace.project.domain.view.ProjectRewardView;
import onlydust.com.marketplace.project.domain.view.ProjectRewardsPageView;

import static onlydust.com.marketplace.api.rest.api.adapter.mapper.MoneyMapper.toMoney;

public interface ProjectRewardMapper {

    static RewardsPageResponse mapProjectRewardPageToResponse(Integer pageIndex, ProjectRewardsPageView page, AuthenticatedUser authenticatedUser) {
        final RewardsPageResponse rewardsPageResponse = new RewardsPageResponse()
                .hasMore(PaginationHelper.hasMore(pageIndex, page.getRewards().getTotalPageNumber()))
                .totalPageNumber(page.getRewards().getTotalPageNumber())
                .totalItemNumber(page.getRewards().getTotalItemNumber())
                .nextPageIndex(PaginationHelper.nextPageIndex(pageIndex, page.getRewards().getTotalPageNumber()))
                .remainingBudget(new DetailedTotalMoney()
                        .totalUsdEquivalent(page.totalRemainingUsdEquivalent())
                        .totalPerCurrency(page.getBudgetStatsPerCurrency().stream()
                                .map(budgetStats -> toMoney(budgetStats.remainingBudget()))
                                .toList()))
                .spentAmount(new DetailedTotalMoney()
                        .totalUsdEquivalent(page.totalSpentUsdEquivalent())
                        .totalPerCurrency(page.getBudgetStatsPerCurrency().stream()
                                .map(budgetStats -> toMoney(budgetStats.spentAmount()))
                                .toList()))
                .sentRewardsCount(page.getSentRewardsCount())
                .rewardedContributionsCount(page.getRewardedContributionsCount())
                .rewardedContributorsCount(page.getRewardedContributorsCount());

        page.getRewards().getContent().stream()
                .map(r -> mapProjectRewardViewToResponse(r, authenticatedUser))
                .forEach(rewardsPageResponse::addRewardsItem);

        return rewardsPageResponse;
    }

    static RewardPageItemResponse mapProjectRewardViewToResponse(final ProjectRewardView view, AuthenticatedUser authenticatedUser) {
        final RewardPageItemResponse rewardPageItemResponse = new RewardPageItemResponse();
        rewardPageItemResponse.setId(view.getId());
        rewardPageItemResponse.setNumberOfRewardedContributions(view.getNumberOfRewardedContributions());
        rewardPageItemResponse.setRewardedUser(ContributorMapper.of(view.getRewardedUser()));
        rewardPageItemResponse.setAmount(toMoney(view.getAmount()));
        rewardPageItemResponse.setStatus(RewardMapper.map(view.getStatus().as(authenticatedUser)));
        rewardPageItemResponse.setRequestedAt(DateMapper.toZoneDateTime(view.getRequestedAt()));
        rewardPageItemResponse.setProcessedAt(DateMapper.toZoneDateTime(view.getProcessedAt()));
        rewardPageItemResponse.setUnlockDate(DateMapper.toZoneDateTime(view.getUnlockDate()));
        return rewardPageItemResponse;
    }
}
