package onlydust.com.marketplace.api.rest.api.adapter.mapper;

import onlydust.com.marketplace.api.contract.model.*;
import onlydust.com.marketplace.api.domain.view.UserRewardTotalAmountsView;
import onlydust.com.marketplace.api.domain.view.UserRewardView;
import onlydust.com.marketplace.api.domain.view.UserTotalRewardView;
import onlydust.com.marketplace.api.domain.view.pagination.Page;
import onlydust.com.marketplace.api.domain.view.pagination.PaginationHelper;

import java.util.List;
import java.util.Objects;

import static onlydust.com.marketplace.api.domain.view.pagination.PaginationHelper.hasMore;

public interface MyRewardMapper {

    static MyRewardsPageResponse mapMyRewardsToResponse(final int pageIndex,
                                                        final Page<UserRewardView> page) {
        final MyRewardsPageResponse myRewardsPageResponse = new MyRewardsPageResponse();
        myRewardsPageResponse.setHasMore(hasMore(pageIndex, page.getTotalPageNumber()));
        myRewardsPageResponse.setTotalPageNumber(page.getTotalPageNumber());
        myRewardsPageResponse.setTotalItemNumber(page.getTotalItemNumber());
        myRewardsPageResponse.setNextPageIndex(PaginationHelper.nextPageIndex(pageIndex, page.getTotalPageNumber()));
        page.getContent().stream()
                .map(MyRewardMapper::mapMyRewardViewToResponse)
                .forEach(myRewardsPageResponse::addRewardsItem);
        return myRewardsPageResponse;
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
                    .currency(switch (userTotalReward.getCurrency()) {
                        case Apt -> CurrencyContract.APT;
                        case Op -> CurrencyContract.OP;
                        case Eth -> CurrencyContract.ETH;
                        case Usd -> CurrencyContract.USD;
                        case Stark -> CurrencyContract.STARK;
                        case Lords -> CurrencyContract.LORDS;
                    }));
        }
        return myRewardTotalAmountsResponse;
    }

    static MyRewardsListResponse listToResponse(final List<UserRewardView> views) {
        return new MyRewardsListResponse()
                .rewards(views.stream().map(MyRewardMapper::mapMyRewardViewToResponse).toList());
    }
}
