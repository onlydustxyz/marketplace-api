package onlydust.com.marketplace.api.rest.api.adapter.mapper;

import onlydust.com.marketplace.api.contract.model.*;
import onlydust.com.marketplace.api.domain.view.RewardableItemView;
import onlydust.com.marketplace.api.domain.view.pagination.Page;
import onlydust.com.marketplace.api.domain.view.pagination.PaginationHelper;

import java.util.List;

import static onlydust.com.marketplace.api.domain.model.ContributionType.*;

public interface RewardableItemMapper {

    static RewardableItemsPageResponse pageToResponse(final int pageIndex, Page<RewardableItemView> page) {
        final RewardableItemsPageResponse rewardableItemsPageResponse = new RewardableItemsPageResponse();
        rewardableItemsPageResponse.setHasMore(PaginationHelper.hasMore(pageIndex, page.getTotalPageNumber()));
        rewardableItemsPageResponse.setTotalPageNumber(page.getTotalPageNumber());
        rewardableItemsPageResponse.setTotalItemNumber(page.getTotalItemNumber());
        rewardableItemsPageResponse.setNextPageIndex(PaginationHelper.nextPageIndex(pageIndex,
                page.getTotalPageNumber()));
        if (!page.getContent().isEmpty()) {
            page.getContent().stream()
                    .map(RewardableItemMapper::itemToResponse)
                    .forEach(rewardableItemsPageResponse::addRewardableItemsItem);
        } else {
            rewardableItemsPageResponse.setRewardableItems(List.of());
        }
        return rewardableItemsPageResponse;
    }

    static RewardableItemResponse itemToResponse(final RewardableItemView view) {
        return new RewardableItemResponse()
                .id(view.getId())
                .contributionId(view.getContributionId())
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
                    case COMMENTED -> GithubStatus.COMMENTED;
                    case APPROVED -> GithubStatus.APPROVED;
                    case CHANGES_REQUESTED -> GithubStatus.CHANGES_REQUESTED;
                    case COMPLETED -> GithubStatus.COMPLETED;
                    case DISMISSED -> GithubStatus.DISMISSED;
                })
                .createdAt(DateMapper.toZoneDateTime(view.getCreatedAt()))
                .completedAt(DateMapper.toZoneDateTime(view.getCompletedAt()))
                .commentsCount(view.getCommentsCount())
                .commitsCount(view.getCommitsCount())
                .userCommitsCount(view.getUserCommitsCount())
                .number(view.getNumber())
                .repoName(view.getRepoName())
                .repoId(view.getRepoId())
                .htmlUrl(view.getGithubUrl())
                .title(view.getTitle())
                .ignored(Boolean.TRUE.equals(view.getIgnored()))
                .githubBody(view.getGithubBody())
                .author(ProjectMapper.mapGithubUser(view.getGithubAuthor()));
    }

    static AllRewardableItemsResponse listToResponse(List<RewardableItemView> rewardableItems) {
        final AllRewardableItemsResponse allRewardableItemsResponse = new AllRewardableItemsResponse();
        allRewardableItemsResponse.setRewardableIssues(rewardableItems.stream().filter(item -> ISSUE.equals(item.getType()))
                .map(RewardableItemMapper::itemToResponse).toList());
        allRewardableItemsResponse.setRewardablePullRequests(rewardableItems.stream().filter(item -> PULL_REQUEST.equals(item.getType()))
                .map(RewardableItemMapper::itemToResponse).toList());
        allRewardableItemsResponse.setRewardableCodeReviews(rewardableItems.stream().filter(item -> CODE_REVIEW.equals(item.getType()))
                .map(RewardableItemMapper::itemToResponse).toList());
        return allRewardableItemsResponse;
    }
}
