package onlydust.com.marketplace.api.rest.api.adapter.mapper;

import onlydust.com.marketplace.api.contract.model.*;
import onlydust.com.marketplace.kernel.pagination.PaginationHelper;
import onlydust.com.marketplace.project.domain.view.ProjectRewardView;
import onlydust.com.marketplace.project.domain.view.ProjectRewardsPageView;

import static java.util.Objects.nonNull;
import static onlydust.com.marketplace.api.rest.api.adapter.mapper.ProjectBudgetMapper.mapCurrency;

public interface ProjectRewardMapper {

    public static Money mapMoney(onlydust.com.marketplace.project.domain.view.Money money) {
        return new Money().amount(money.getAmount())
                .currency(nonNull(money.getCurrency()) ? mapCurrency(money.getCurrency()) : null)
                .usdEquivalent(money.getUsdEquivalent());
    }

    static RewardsPageResponse mapProjectRewardPageToResponse(Integer pageIndex, ProjectRewardsPageView page) {
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
                .map(ProjectRewardMapper::mapProjectRewardViewToResponse)
                .forEach(rewardsPageResponse::addRewardsItem);

        return rewardsPageResponse;
    }

    static RewardPageItemResponse mapProjectRewardViewToResponse(final ProjectRewardView view) {
        final RewardPageItemResponse rewardPageItemResponse = new RewardPageItemResponse();
        rewardPageItemResponse.setId(view.getId());
        rewardPageItemResponse.setNumberOfRewardedContributions(view.getNumberOfRewardedContributions());
        rewardPageItemResponse.setRewardedUserLogin(view.getRewardedUserLogin());
        rewardPageItemResponse.setRewardedUserAvatar(view.getRewardedUserAvatar());
        final RewardAmountResponse amount = mapRewardAmountToResponse(view);
        rewardPageItemResponse.setAmount(amount);
        rewardPageItemResponse.setStatus(switch (view.getStatus()) {
            case complete -> RewardStatus.COMPLETE;
            case pendingSignup -> RewardStatus.PENDING_SIGNUP;
            case processing -> RewardStatus.PROCESSING;
            case locked -> RewardStatus.LOCKED;
            case pendingContributor -> RewardStatus.PENDING_CONTRIBUTOR;
        });
        rewardPageItemResponse.setRequestedAt(DateMapper.toZoneDateTime(view.getRequestedAt()));
        rewardPageItemResponse.setProcessedAt(DateMapper.toZoneDateTime(view.getProcessedAt()));
        rewardPageItemResponse.setUnlockDate(DateMapper.toZoneDateTime(view.getUnlockDate()));
        return rewardPageItemResponse;
    }

    static RewardAmountResponse mapRewardAmountToResponse(ProjectRewardView view) {
        final RewardAmountResponse amount = new RewardAmountResponse();
        amount.setCurrency(mapCurrency(view.getAmount().getCurrency()));
        amount.setDollarsEquivalent(view.getAmount().getDollarsEquivalent());
        amount.setTotal(view.getAmount().getTotal());
        return amount;
    }
}
