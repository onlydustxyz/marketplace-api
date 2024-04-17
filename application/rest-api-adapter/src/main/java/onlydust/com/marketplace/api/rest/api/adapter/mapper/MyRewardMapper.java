package onlydust.com.marketplace.api.rest.api.adapter.mapper;

import onlydust.com.marketplace.api.contract.model.MyRewardPageItemResponse;
import onlydust.com.marketplace.api.contract.model.MyRewardsPageResponse;
import onlydust.com.marketplace.kernel.model.AuthenticatedUser;
import onlydust.com.marketplace.kernel.pagination.PaginationHelper;
import onlydust.com.marketplace.project.domain.view.UserRewardView;
import onlydust.com.marketplace.project.domain.view.UserRewardsPageView;

import static onlydust.com.marketplace.api.rest.api.adapter.mapper.MoneyMapper.toMoney;
import static onlydust.com.marketplace.kernel.pagination.PaginationHelper.hasMore;

public interface MyRewardMapper {

    static MyRewardsPageResponse mapMyRewardsToResponse(final int pageIndex,
                                                        final UserRewardsPageView page, AuthenticatedUser authenticatedUser) {
        return new MyRewardsPageResponse()
                .hasMore(hasMore(pageIndex, page.getRewards().getTotalPageNumber()))
                .totalPageNumber(page.getRewards().getTotalPageNumber())
                .totalItemNumber(page.getRewards().getTotalItemNumber())
                .nextPageIndex(PaginationHelper.nextPageIndex(pageIndex, page.getRewards().getTotalPageNumber()))
                .rewards(page.getRewards().getContent().stream().map((UserRewardView view) -> mapMyRewardViewToResponse(view, authenticatedUser)).toList())
                .pendingAmount(toMoney(page.getPendingAmount()))
                .rewardedAmount(toMoney(page.getRewardedAmount()))
                .receivedRewardsCount(page.getReceivedRewardsCount())
                .rewardedContributionsCount(page.getRewardedContributionsCount())
                .rewardingProjectsCount(page.getRewardingProjectsCount())
                .pendingRequestCount(page.getPendingRequestCount())
                ;
    }

    static MyRewardPageItemResponse mapMyRewardViewToResponse(final UserRewardView view, AuthenticatedUser authenticatedUser) {
        return new MyRewardPageItemResponse()
                .id(view.getId())
                .projectId(view.getProjectId())
                .numberOfRewardedContributions(view.getNumberOfRewardedContributions())
                .rewardedOnProjectLogoUrl(view.getRewardedOnProjectLogoUrl())
                .rewardedOnProjectName(view.getRewardedOnProjectName())
                .amount(toMoney(view.getAmount()))
                .rewardedUser(ContributorMapper.of(view.getRewardedUser()))
                .status(RewardMapper.map(view.getStatus().as(authenticatedUser)))
                .requestedAt(DateMapper.toZoneDateTime(view.getRequestedAt()))
                .processedAt(DateMapper.toZoneDateTime(view.getProcessedAt()))
                .unlockDate(DateMapper.toZoneDateTime(view.getUnlockDate()))
                .billingProfileId(view.getBillingProfileId())
                ;
    }
}
