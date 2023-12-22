package onlydust.com.marketplace.api.rest.api.adapter.mapper;

import onlydust.com.marketplace.api.contract.model.*;
import onlydust.com.marketplace.api.domain.view.UserRewardTotalAmountsView;
import onlydust.com.marketplace.api.domain.view.UserRewardView;
import onlydust.com.marketplace.api.domain.view.UserRewardsPageView;
import onlydust.com.marketplace.api.domain.view.UserTotalRewardView;
import onlydust.com.marketplace.api.domain.view.pagination.Page;
import onlydust.com.marketplace.api.domain.view.pagination.PaginationHelper;

import java.util.List;
import java.util.Objects;

import static onlydust.com.marketplace.api.domain.view.pagination.PaginationHelper.hasMore;
import static onlydust.com.marketplace.api.rest.api.adapter.mapper.ProjectBudgetMapper.mapCurrency;
import static onlydust.com.marketplace.api.rest.api.adapter.mapper.ProjectRewardMapper.mapMoney;

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
        final MyRewardPageItemResponse myRewardPageItemResponse = new MyRewardPageItemResponse();
        myRewardPageItemResponse.setId(view.getId());
        myRewardPageItemResponse.setProjectId(view.getProjectId());
        myRewardPageItemResponse.setNumberOfRewardedContributions(view.getNumberOfRewardedContributions());
        myRewardPageItemResponse.setRewardedOnProjectLogoUrl(view.getRewardedOnProjectLogoUrl());
        myRewardPageItemResponse.setRewardedOnProjectName(view.getRewardedOnProjectName());
        final RewardAmountResponse amount = mapRewardAmountToResponse(view);
        myRewardPageItemResponse.setAmount(amount);
        myRewardPageItemResponse.setStatus(switch (view.getStatus()) {
            case complete -> RewardStatus.COMPLETE;
            case pendingInvoice -> RewardStatus.PENDING_INVOICE;
            case processing -> RewardStatus.PROCESSING;
            case missingPayoutInfo -> RewardStatus.MISSING_PAYOUT_INFO;
        });
        myRewardPageItemResponse.setRequestedAt(DateMapper.toZoneDateTime(view.getRequestedAt()));
        return myRewardPageItemResponse;
    }

    private static RewardAmountResponse mapRewardAmountToResponse(UserRewardView view) {
        final RewardAmountResponse amount = new RewardAmountResponse();
        amount.setCurrency(mapCurrency(view.getAmount().getCurrency()));
        amount.setDollarsEquivalent(view.getAmount().getDollarsEquivalent());
        amount.setTotal(view.getAmount().getTotal());
        return amount;
    }


    static UserRewardView.SortBy getSortBy(String sort) {
        sort = Objects.isNull(sort) ? "" : sort;
        return switch (sort) {
            case "STATUS" -> UserRewardView.SortBy.status;
            case "AMOUNT" -> UserRewardView.SortBy.amount;
            case "CONTRIBUTION" -> UserRewardView.SortBy.contribution;
            default -> UserRewardView.SortBy.requestedAt;
        };
    }


    static RewardTotalAmountsResponse mapUserRewardTotalAmountsToResponse(final UserRewardTotalAmountsView view) {
        final RewardTotalAmountsResponse myRewardTotalAmountsResponse = new RewardTotalAmountsResponse();
        myRewardTotalAmountsResponse.setTotalAmount(view.getTotalAmount());
        for (UserTotalRewardView userTotalReward : view.getUserTotalRewards()) {
            myRewardTotalAmountsResponse.addDetailsItem(new MyRewardAmountResponse().totalAmount(userTotalReward.getTotalAmount())
                    .totalDollarsEquivalent(userTotalReward.getTotalDollarsEquivalent())
                    .currency(mapCurrency (userTotalReward.getCurrency())));
        }
        return myRewardTotalAmountsResponse;
    }

    static MyRewardsListResponse listToResponse(final List<UserRewardView> views) {
        return new MyRewardsListResponse()
                .rewards(views.stream().map(MyRewardMapper::mapMyRewardViewToResponse).toList());
    }
}
