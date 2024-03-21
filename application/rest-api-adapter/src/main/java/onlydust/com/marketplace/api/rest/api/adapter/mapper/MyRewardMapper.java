package onlydust.com.marketplace.api.rest.api.adapter.mapper;

import onlydust.com.marketplace.api.contract.model.MyRewardPageItemResponse;
import onlydust.com.marketplace.api.contract.model.MyRewardsPageResponse;
import onlydust.com.marketplace.api.contract.model.RewardAmountResponse;
import onlydust.com.marketplace.kernel.pagination.PaginationHelper;
import onlydust.com.marketplace.project.domain.view.BillingProfileLinkView;
import onlydust.com.marketplace.project.domain.view.UserRewardView;
import onlydust.com.marketplace.project.domain.view.UserRewardsPageView;

import java.util.List;

import static onlydust.com.marketplace.api.rest.api.adapter.mapper.ProjectRewardMapper.mapMoney;
import static onlydust.com.marketplace.api.rest.api.adapter.mapper.RewardMapper.mapCurrency;
import static onlydust.com.marketplace.kernel.pagination.PaginationHelper.hasMore;

public interface MyRewardMapper {

    static MyRewardsPageResponse mapMyRewardsToResponse(final int pageIndex,
                                                        final UserRewardsPageView page, Long githubUserId,
                                                        List<BillingProfileLinkView> billingProfiles) {
        return new MyRewardsPageResponse()
                .hasMore(hasMore(pageIndex, page.getRewards().getTotalPageNumber()))
                .totalPageNumber(page.getRewards().getTotalPageNumber())
                .totalItemNumber(page.getRewards().getTotalItemNumber())
                .nextPageIndex(PaginationHelper.nextPageIndex(pageIndex, page.getRewards().getTotalPageNumber()))
                .rewards(page.getRewards().getContent().stream().map((UserRewardView view) -> mapMyRewardViewToResponse(view, githubUserId, billingProfiles)).toList())
                .pendingAmount(mapMoney(page.getPendingAmount()))
                .rewardedAmount(mapMoney(page.getRewardedAmount()))
                .receivedRewardsCount(page.getReceivedRewardsCount())
                .rewardedContributionsCount(page.getRewardedContributionsCount())
                .rewardingProjectsCount(page.getRewardingProjectsCount())
                ;
    }

    static MyRewardPageItemResponse mapMyRewardViewToResponse(final UserRewardView view, Long githubUserId,
                                                              List<BillingProfileLinkView> billingProfiles) {
        return new MyRewardPageItemResponse()
                .id(view.getId())
                .projectId(view.getProjectId())
                .numberOfRewardedContributions(view.getNumberOfRewardedContributions())
                .rewardedOnProjectLogoUrl(view.getRewardedOnProjectLogoUrl())
                .rewardedOnProjectName(view.getRewardedOnProjectName())
                .amount(mapRewardAmountToResponse(view))
                .status(RewardMapper.map(view.getStatus().getRewardStatusForUser(view.getId(), view.getStatus(),
                        view.getRecipientId(), view.getBillingProfileId(), githubUserId,
                        billingProfiles.stream().map(BillingProfileLinkView::toUserBillingProfile).toList())))
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

}
