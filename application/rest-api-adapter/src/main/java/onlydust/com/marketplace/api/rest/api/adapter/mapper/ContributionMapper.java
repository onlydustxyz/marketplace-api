package onlydust.com.marketplace.api.rest.api.adapter.mapper;

import onlydust.com.marketplace.api.contract.model.*;
import onlydust.com.marketplace.api.domain.model.ContributionStatus;
import onlydust.com.marketplace.api.domain.model.ContributionType;
import onlydust.com.marketplace.api.domain.view.*;
import onlydust.com.marketplace.api.domain.view.pagination.Page;
import onlydust.com.marketplace.api.domain.view.pagination.PaginationHelper;

import java.util.Optional;

import static onlydust.com.marketplace.api.domain.view.pagination.PaginationHelper.hasMore;

public interface ContributionMapper {
    static ContributionType mapContributionType(onlydust.com.marketplace.api.contract.model.ContributionType type) {
        return switch (type) {
            case CODE_REVIEW -> ContributionType.CODE_REVIEW;
            case ISSUE -> ContributionType.ISSUE;
            case PULL_REQUEST -> ContributionType.PULL_REQUEST;
        };
    }

    static ContributionStatus mapContributionStatus(onlydust.com.marketplace.api.contract.model.ContributionStatus status) {
        return switch (status) {
            case IN_PROGRESS -> ContributionStatus.IN_PROGRESS;
            case COMPLETED -> ContributionStatus.COMPLETED;
            case CANCELLED -> ContributionStatus.CANCELLED;
        };
    }


    static UserContributionPageResponse mapUserContributionPageResponse(Integer pageIndex,
                                                                        Page<ContributionView> contributions) {

        return new UserContributionPageResponse()
                .contributions(contributions.getContent().stream().map(ContributionMapper::mapContributionPageItemResponse).toList())
                .hasMore(hasMore(pageIndex, contributions.getTotalPageNumber()))
                .totalPageNumber(contributions.getTotalPageNumber())
                .totalItemNumber(contributions.getTotalItemNumber())
                .nextPageIndex(PaginationHelper.nextPageIndex(pageIndex, contributions.getTotalPageNumber()));
    }

    static ContributionPageItemResponse mapContributionPageItemResponse(ContributionView contributionView) {
        return new ContributionPageItemResponse()
                .id(contributionView.getId())
                .contributor(ContributorMapper.of(contributionView.getContributor()))
                .createdAt(DateMapper.toZoneDateTime(contributionView.getCreatedAt()))
                .completedAt(DateMapper.toZoneDateTime(contributionView.getCompletedAt()))
                .type(mapContributionTypeToResponse(contributionView.getType()))
                .status(ContributionMapper.mapContributionStatusToResponse(contributionView.getStatus()))
                .githubNumber(contributionView.getGithubNumber())
                .githubStatus(GithubStatus.valueOf(contributionView.getGithubStatus()))
                .githubTitle(contributionView.getGithubTitle())
                .githubHtmlUrl(contributionView.getGithubHtmlUrl())
                .githubBody(contributionView.getGithubBody())
                .githubAuthor(ProjectMapper.mapGithubUser(contributionView.getGithubAuthor()))
                .githubPullRequestReviewState(Optional.ofNullable(contributionView.getPrReviewState()).map(ContributionMapper::mapGithubPullRequestReviewState).orElse(null))
                .project(ProjectMapper.mapShortProjectResponse(contributionView.getProject()))
                .repo(GithubRepoMapper.mapRepoToShortResponse(contributionView.getGithubRepo()))
                .links(contributionView.getLinks().stream().map(ContributionMapper::mapContributionLink).toList())
                .rewardIds(contributionView.getRewardIds());
    }

    static ProjectStaledContributionsPageItemResponse mapProjectStaledContributionsPageItemResponse(ContributionView contributionView) {
        return new ProjectStaledContributionsPageItemResponse()
                .id(contributionView.getId())
                .contributor(ContributorMapper.of(contributionView.getContributor()))
                .createdAt(DateMapper.toZoneDateTime(contributionView.getCreatedAt()))
                .type(mapContributionTypeToResponse(contributionView.getType()))
                .status(ContributionMapper.mapContributionStatusToResponse(contributionView.getStatus()))
                .githubNumber(contributionView.getGithubNumber())
                .githubStatus(GithubStatus.valueOf(contributionView.getGithubStatus()))
                .githubTitle(contributionView.getGithubTitle())
                .githubHtmlUrl(contributionView.getGithubHtmlUrl())
                .githubBody(contributionView.getGithubBody())
                .repo(GithubRepoMapper.mapRepoToShortResponse(contributionView.getGithubRepo()));
    }

    static GithubPullRequestReviewState mapGithubPullRequestReviewState(PullRequestReviewState githubPullRequestReviewState) {
        return switch (githubPullRequestReviewState) {
            case APPROVED -> GithubPullRequestReviewState.APPROVED;
            case CHANGES_REQUESTED -> GithubPullRequestReviewState.CHANGES_REQUESTED;
            case PENDING_REVIEWER -> GithubPullRequestReviewState.PENDING_REVIEWER;
            case UNDER_REVIEW -> GithubPullRequestReviewState.UNDER_REVIEW;
        };
    }

    static ContributionLinkResponse mapContributionLink(ContributionLinkView link) {
        return new ContributionLinkResponse()
                .type(mapContributionTypeToResponse(link.getType()))
                .githubNumber(link.getGithubNumber())
                .githubStatus(GithubStatus.valueOf(link.getGithubStatus()))
                .githubTitle(link.getGithubTitle())
                .githubHtmlUrl(link.getGithubHtmlUrl())
                .githubBody(link.getGithubBody())
                .githubAuthor(ProjectMapper.mapGithubUser(link.getGithubAuthor()))
                .repo(GithubRepoMapper.mapRepoToShortResponse(link.getGithubRepo()))
                .isMine(link.getIsMine());
    }

    static onlydust.com.marketplace.api.contract.model.ContributionStatus mapContributionStatusToResponse(onlydust.com.marketplace.api.domain.model.ContributionStatus status) {
        return switch (status) {
            case COMPLETED -> onlydust.com.marketplace.api.contract.model.ContributionStatus.COMPLETED;
            case IN_PROGRESS -> onlydust.com.marketplace.api.contract.model.ContributionStatus.IN_PROGRESS;
            case CANCELLED -> onlydust.com.marketplace.api.contract.model.ContributionStatus.CANCELLED;
        };
    }

    static onlydust.com.marketplace.api.contract.model.ContributionType mapContributionTypeToResponse(ContributionType type) {
        return switch (type) {
            case CODE_REVIEW -> onlydust.com.marketplace.api.contract.model.ContributionType.CODE_REVIEW;
            case ISSUE -> onlydust.com.marketplace.api.contract.model.ContributionType.ISSUE;
            case PULL_REQUEST -> onlydust.com.marketplace.api.contract.model.ContributionType.PULL_REQUEST;
        };
    }

    static ContributionView.Sort mapSort(ContributionSort sort) {
        return switch (sort) {
            case CREATED_AT -> ContributionView.Sort.CREATED_AT;
            case PROJECT_REPO_NAME -> ContributionView.Sort.PROJECT_REPO_NAME;
            case GITHUB_NUMBER_TITLE -> ContributionView.Sort.GITHUB_NUMBER_TITLE;
            case LINKS_COUNT -> ContributionView.Sort.LINKS_COUNT;
        };
    }

    static ContributionView.Sort mapSort(ProjectContributionSort sort) {
        return switch (sort) {
            case CREATED_AT -> ContributionView.Sort.CREATED_AT;
            case GITHUB_NUMBER_TITLE -> ContributionView.Sort.GITHUB_NUMBER_TITLE;
            case LINKS_COUNT -> ContributionView.Sort.LINKS_COUNT;
            case REPO_NAME -> ContributionView.Sort.PROJECT_REPO_NAME;
            case CONTRIBUTOR_LOGIN -> ContributionView.Sort.CONTRIBUTOR_LOGIN;
        };
    }

    static ContributionDetailsResponse mapContributionDetails(ContributionDetailsView contribution) {
        return new ContributionDetailsResponse()
                .id(contribution.getId())
                .contributor(ContributorMapper.of(contribution.getContributor()))
                .createdAt(DateMapper.toZoneDateTime(contribution.getCreatedAt()))
                .completedAt(DateMapper.toZoneDateTime(contribution.getCompletedAt()))
                .type(mapContributionTypeToResponse(contribution.getType()))
                .status(ContributionMapper.mapContributionStatusToResponse(contribution.getStatus()))
                .githubNumber(contribution.getGithubNumber())
                .githubStatus(GithubStatus.valueOf(contribution.getGithubStatus()))
                .githubTitle(contribution.getGithubTitle())
                .githubHtmlUrl(contribution.getGithubHtmlUrl())
                .githubBody(contribution.getGithubBody())
                .githubAuthor(ProjectMapper.mapGithubUser(contribution.getGithubAuthor()))
                .githubPullRequestReviewState(Optional.ofNullable(contribution.getPrReviewState()).map(ContributionMapper::mapGithubPullRequestReviewState).orElse(null))
                .project(ProjectMapper.mapShortProjectResponse(contribution.getProject()))
                .repo(GithubRepoMapper.mapRepoToShortResponse(contribution.getGithubRepo()))
                .commentsCount(contribution.getGithubCommentsCount())
                .commitsCount(contribution.getGithubCommitsCount())
                .userCommitsCount(contribution.getGithubUserCommitsCount())
                .links(contribution.getLinks().stream().map(ContributionMapper::mapContributionLink).toList())
                .rewards(contribution.getRewards().stream().map(RewardMapper::rewardToResponse).toList());
    }

    static ProjectContributionPageResponse mapProjectContributionPageResponse(Integer pageIndex, Page<ContributionView> contributions) {
        return new ProjectContributionPageResponse()
                .contributions(contributions.getContent().stream().map(ContributionMapper::mapContributionPageItemResponse).toList())
                .hasMore(hasMore(pageIndex, contributions.getTotalPageNumber()))
                .totalPageNumber(contributions.getTotalPageNumber())
                .totalItemNumber(contributions.getTotalItemNumber())
                .nextPageIndex(PaginationHelper.nextPageIndex(pageIndex, contributions.getTotalPageNumber()));
    }

    static ProjectStaledContributionsPageResponse mapProjectStaledContributionsPageResponse(Integer pageIndex, Page<ContributionView> contributions) {
        return new ProjectStaledContributionsPageResponse()
                .contributions(contributions.getContent().stream().map(ContributionMapper::mapProjectStaledContributionsPageItemResponse).toList())
                .hasMore(hasMore(pageIndex, contributions.getTotalPageNumber()))
                .totalPageNumber(contributions.getTotalPageNumber())
                .totalItemNumber(contributions.getTotalItemNumber())
                .nextPageIndex(PaginationHelper.nextPageIndex(pageIndex, contributions.getTotalPageNumber()));
    }
}
