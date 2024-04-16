package onlydust.com.marketplace.api.rest.api.adapter.mapper;

import onlydust.com.marketplace.api.contract.model.Money;
import onlydust.com.marketplace.api.contract.model.RewardAmountResponse;
import onlydust.com.marketplace.api.contract.model.RewardPageItemResponse;
import onlydust.com.marketplace.api.contract.model.RewardsPageResponse;
import onlydust.com.marketplace.kernel.model.AuthenticatedUser;
import onlydust.com.marketplace.kernel.pagination.PaginationHelper;
import onlydust.com.marketplace.project.domain.view.ProjectRewardView;
import onlydust.com.marketplace.project.domain.view.ProjectRewardsPageView;

import static java.util.Objects.nonNull;
import static onlydust.com.marketplace.api.rest.api.adapter.mapper.RewardMapper.mapCurrency;

public interface ProjectRewardMapper {

    public static Money mapMoney(onlydust.com.marketplace.project.domain.view.Money money) {
        return new Money().amount(money.getAmount())
                .currency(nonNull(money.getCurrency()) ? mapCurrency(money.getCurrency()) : null)
                .usdEquivalent(money.getUsdEquivalent());
    }

    static RewardsPageResponse mapProjectRewardPageToResponse(Integer pageIndex, ProjectRewardsPageView page, AuthenticatedUser authenticatedUser) {
        final RewardsPageResponse rewardsPageResponse = new RewardsPageResponse()
                .hasMore(PaginationHelper.hasMore(pageIndex, page.getRewards().getTotalPageNumber()))
                .totalPageNumber(page.getRewards().getTotalPageNumber())
                .totalItemNumber(page.getRewards().getTotalItemNumber())
                .nextPageIndex(PaginationHelper.nextPageIndex(pageIndex, page.getRewards().getTotalPageNumber()))
                .remainingBudget(mapMoney(page.getRemainingBudget()))
                .spentAmount(mapMoney(page.getSpentAmount()))
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
        final RewardAmountResponse amount = mapRewardAmountToResponse(view);
        rewardPageItemResponse.setAmount(amount);
        rewardPageItemResponse.setStatus(RewardMapper.map(view.getStatus().as(authenticatedUser)));
        rewardPageItemResponse.setRequestedAt(DateMapper.toZoneDateTime(view.getRequestedAt()));
        rewardPageItemResponse.setProcessedAt(DateMapper.toZoneDateTime(view.getProcessedAt()));
        rewardPageItemResponse.setUnlockDate(DateMapper.toZoneDateTime(view.getUnlockDate()));
        return rewardPageItemResponse;
    }

    static RewardAmountResponse mapRewardAmountToResponse(ProjectRewardView view) {
        return new RewardAmountResponse()
                .currency(mapCurrency(view.getAmount().getCurrency()))
                .dollarsEquivalent(view.getAmount().getUsdEquivalent())
                .total(view.getAmount().getAmount());
    }
}
