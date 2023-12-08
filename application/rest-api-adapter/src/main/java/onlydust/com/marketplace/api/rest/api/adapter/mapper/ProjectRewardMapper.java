package onlydust.com.marketplace.api.rest.api.adapter.mapper;

import onlydust.com.marketplace.api.contract.model.*;
import onlydust.com.marketplace.api.domain.view.ProjectRewardView;
import onlydust.com.marketplace.api.domain.view.ProjectRewardsPageView;
import onlydust.com.marketplace.api.domain.view.pagination.PaginationHelper;

import java.util.Objects;

import static java.util.Objects.nonNull;

public interface ProjectRewardMapper {

    private static Money mapMoney(ProjectRewardsPageView.Money money) {
        return new Money().amount(money.getAmount())
                .currency(nonNull(money.getCurrency()) ? ProjectBudgetMapper.mapCurrency(money.getCurrency()) : null)
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
        });
        rewardPageItemResponse.setRequestedAt(DateMapper.toZoneDateTime(view.getRequestedAt()));
        return rewardPageItemResponse;
    }

    static RewardAmountResponse mapRewardAmountToResponse(ProjectRewardView view) {
        final RewardAmountResponse amount = new RewardAmountResponse();
        amount.setCurrency(switch (view.getAmount().getCurrency()) {
            case Apt -> CurrencyContract.APT;
            case Op -> CurrencyContract.OP;
            case Eth -> CurrencyContract.ETH;
            case Stark -> CurrencyContract.STARK;
            case Usd -> CurrencyContract.USD;
            case Lords -> CurrencyContract.LORDS;
        });
        amount.setDollarsEquivalent(view.getAmount().getDollarsEquivalent());
        amount.setTotal(view.getAmount().getTotal());
        return amount;
    }


    static ProjectRewardView.SortBy getSortBy(String sort) {
        sort = Objects.isNull(sort) ? "" : sort;
        return switch (sort) {
            case "AMOUNT" -> ProjectRewardView.SortBy.amount;
            case "CONTRIBUTION" -> ProjectRewardView.SortBy.contribution;
            case "STATUS" -> ProjectRewardView.SortBy.status;
            default -> ProjectRewardView.SortBy.requestedAt;
        };
    }
}
