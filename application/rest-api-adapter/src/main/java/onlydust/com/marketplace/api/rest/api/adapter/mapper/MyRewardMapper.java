package onlydust.com.marketplace.api.rest.api.adapter.mapper;

import onlydust.com.marketplace.api.contract.model.*;
import onlydust.com.marketplace.kernel.pagination.PaginationHelper;
import onlydust.com.marketplace.project.domain.view.UserRewardTotalAmountsView;
import onlydust.com.marketplace.project.domain.view.UserRewardView;
import onlydust.com.marketplace.project.domain.view.UserRewardsPageView;
import onlydust.com.marketplace.project.domain.view.UserTotalRewardView;

import java.util.List;

import static onlydust.com.marketplace.api.rest.api.adapter.mapper.ProjectBudgetMapper.mapCurrency;
import static onlydust.com.marketplace.api.rest.api.adapter.mapper.ProjectRewardMapper.mapMoney;
import static onlydust.com.marketplace.kernel.pagination.PaginationHelper.hasMore;

public interface MyRewardMapper {

    static MyRewardsPageResponse mapMyRewardsToResponse(final int pageIndex,
                                                        final UserRewardsPageView page) {
        return new MyRewardsPageResponse()
                .hasMore(hasMore(pageIndex, page.getRewards().getTotalPageNumber()))
                .totalPageNumber(page.getRewards().getTotalPageNumber())
                .totalItemNumber(page.getRewards().getTotalItemNumber())
                .nextPageIndex(PaginationHelper.nextPageIndex(pageIndex, page.getRewards().getTotalPageNumber()))
                .rewards(page.getRewards().getContent().stream().map(MyRewardMapper::mapMyRewardViewToResponse).toList())
                .pendingAmount(mapMoney(page.getPendingAmount()))
                .rewardedAmount(mapMoney(page.getRewardedAmount()))
                .receivedRewardsCount(page.getReceivedRewardsCount())
                .rewardedContributionsCount(page.getRewardedContributionsCount())
                .rewardingProjectsCount(page.getRewardingProjectsCount())
                ;
    }

    static MyRewardPageItemResponse mapMyRewardViewToResponse(final UserRewardView view) {
        return new MyRewardPageItemResponse()
                .id(view.getId())
                .projectId(view.getProjectId())
                .numberOfRewardedContributions(view.getNumberOfRewardedContributions())
                .rewardedOnProjectLogoUrl(view.getRewardedOnProjectLogoUrl())
                .rewardedOnProjectName(view.getRewardedOnProjectName())
                .amount(mapRewardAmountToResponse(view))
                .status(RewardMapper.map(view.getStatus().asUser()))
                .requestedAt(DateMapper.toZoneDateTime(view.getRequestedAt()))
                .processedAt(DateMapper.toZoneDateTime(view.getProcessedAt()))
                .unlockDate(DateMapper.toZoneDateTime(view.getUnlockDate()))
                ;
    }

    private static RewardAmountResponse mapRewardAmountToResponse(UserRewardView view) {
        return new RewardAmountResponse()
                .currency(mapCurrency(view.getAmount().getCurrency()))
                .dollarsEquivalent(view.getAmount().getDollarsEquivalent())
                .total(view.getAmount().getTotal());
    }

    static RewardTotalAmountsResponse mapUserRewardTotalAmountsToResponse(final UserRewardTotalAmountsView view) {
        final RewardTotalAmountsResponse myRewardTotalAmountsResponse = new RewardTotalAmountsResponse();
        myRewardTotalAmountsResponse.setTotalAmount(view.getTotalAmount());
        for (UserTotalRewardView userTotalReward : view.getUserTotalRewards()) {
            myRewardTotalAmountsResponse.addDetailsItem(new MyRewardAmountResponse().totalAmount(userTotalReward.getTotalAmount())
                    .totalDollarsEquivalent(userTotalReward.getTotalDollarsEquivalent())
                    .currency(mapCurrency(userTotalReward.getCurrency())));
        }
        return myRewardTotalAmountsResponse;
    }

    static MyRewardsListResponse listToResponse(final List<UserRewardView> views) {
        return new MyRewardsListResponse()
                .rewards(views.stream().map(MyRewardMapper::mapMyRewardViewToResponse).toList());
    }
}
