package onlydust.com.marketplace.api.rest.api.adapter.mapper;

import onlydust.com.marketplace.api.contract.model.*;
import onlydust.com.marketplace.api.domain.view.ProjectRewardView;
import onlydust.com.marketplace.api.domain.view.pagination.Page;
import onlydust.com.marketplace.api.domain.view.pagination.PaginationHelper;

public interface ProjectRewardMapper {

    static RewardsPageResponse mapProjectRewardPageToResponse(Integer pageIndex, Page<ProjectRewardView> page) {
        final RewardsPageResponse rewardsPageResponse = new RewardsPageResponse();
        rewardsPageResponse.setHasMore(PaginationHelper.hasMore(pageIndex, page.getTotalPageNumber()));
        rewardsPageResponse.setTotalPageNumber(page.getTotalPageNumber());
        rewardsPageResponse.setTotalItemNumber(page.getTotalItemNumber());
        page.getContent().stream()
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
            case Apt -> Currency.APT;
            case Op -> Currency.OP;
            case Eth -> Currency.ETH;
            case Stark -> Currency.STARK;
            case Usd -> Currency.USD;
        });
        amount.setDollarsEquivalent(view.getAmount().getDollarsEquivalent());
        amount.setTotal(view.getAmount().getTotal());
        return amount;
    }


    static ProjectRewardView.SortBy getSortBy(String sort) {
        final ProjectRewardView.SortBy sortBy = switch (sort) {
            case "AMOUNT" -> ProjectRewardView.SortBy.amount;
            case "SENT_AT" -> ProjectRewardView.SortBy.sentAt;
            case "STATUS" -> ProjectRewardView.SortBy.status;
            default -> ProjectRewardView.SortBy.contributor;
        };
        return sortBy;
    }
}
