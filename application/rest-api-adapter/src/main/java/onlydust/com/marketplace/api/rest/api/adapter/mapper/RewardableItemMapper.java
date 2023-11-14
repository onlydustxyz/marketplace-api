package onlydust.com.marketplace.api.rest.api.adapter.mapper;

import onlydust.com.marketplace.api.contract.model.ContributionType;
import onlydust.com.marketplace.api.contract.model.GithubStatus;
import onlydust.com.marketplace.api.contract.model.RewardableItemResponse;
import onlydust.com.marketplace.api.contract.model.RewardableItemsPageResponse;
import onlydust.com.marketplace.api.domain.view.RewardItemView;
import onlydust.com.marketplace.api.domain.view.pagination.Page;
import onlydust.com.marketplace.api.domain.view.pagination.PaginationHelper;

import static java.util.Objects.isNull;

public interface RewardableItemMapper {

    static RewardableItemsPageResponse pageToResponse(final int pageIndex, Page<RewardItemView> page) {
        final RewardableItemsPageResponse rewardableItemsPageResponse = new RewardableItemsPageResponse();
        rewardableItemsPageResponse.setHasMore(PaginationHelper.hasMore(pageIndex, page.getTotalPageNumber()));
        rewardableItemsPageResponse.setTotalPageNumber(page.getTotalPageNumber());
        rewardableItemsPageResponse.setTotalItemNumber(page.getTotalItemNumber());
        rewardableItemsPageResponse.setNextPageIndex(PaginationHelper.nextPageIndex(pageIndex,
                page.getTotalPageNumber()));
        page.getContent().stream()
                .map(RewardableItemMapper::itemToResponse)
                .forEach(rewardableItemsPageResponse::addRewardItemsItem);
        return rewardableItemsPageResponse;
    }

    private static RewardableItemResponse itemToResponse(final RewardItemView view) {
        return new RewardableItemResponse()
                .id(view.getId())
                .type(switch (view.getType()) {
                    case ISSUE -> ContributionType.ISSUE;
                    case CODE_REVIEW -> ContributionType.CODE_REVIEW;
                    case PULL_REQUEST -> ContributionType.PULL_REQUEST;
                })
                .status(switch (view.getStatus()) {
                    case CANCELLED -> GithubStatus.CANCELLED;
                    case PENDING -> GithubStatus.PENDING;
                    case DRAFT -> GithubStatus.DRAFT;
                    case OPEN -> GithubStatus.OPEN;
                    case MERGED -> GithubStatus.MERGED;
                    case CLOSED -> GithubStatus.CLOSED;
                    case CHANGES_REQUESTED -> GithubStatus.CHANGES_REQUESTED;
                    case COMPLETED -> GithubStatus.COMPLETED;
                })
                .createdAt(DateMapper.toZoneDateTime(view.getCreatedAt()))
                .lastUpdateAt(DateMapper.toZoneDateTime(view.getLastUpdateAt()))
                .commentsCount(view.getCommentsCount())
                .commitsCount(view.getCommitsCount())
                .userCommitsCount(view.getUserCommitsCount())
                .number(view.getNumber())
                .repoName(view.getRepoName())
                .githubUrl(view.getGithubUrl())
                .title(view.getTitle())
                .codeReviewOutcome(isNull(view.getOutcome()) ? null : switch (view.getOutcome()) {
                    case approved -> RewardableItemResponse.CodeReviewOutcomeEnum.APPROVED;
                    case changeRequested -> RewardableItemResponse.CodeReviewOutcomeEnum.CHANGE_REQUESTED;
                });
    }
}
