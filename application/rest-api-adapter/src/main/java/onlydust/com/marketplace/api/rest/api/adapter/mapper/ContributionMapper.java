package onlydust.com.marketplace.api.rest.api.adapter.mapper;

import onlydust.com.marketplace.api.contract.model.*;
import onlydust.com.marketplace.api.domain.model.ContributionStatus;
import onlydust.com.marketplace.api.domain.model.ContributionType;
import onlydust.com.marketplace.api.domain.model.GithubRepo;
import onlydust.com.marketplace.api.domain.model.Project;
import onlydust.com.marketplace.api.domain.view.ContributionDetailsView;
import onlydust.com.marketplace.api.domain.view.ContributionLinkView;
import onlydust.com.marketplace.api.domain.view.ContributionView;
import onlydust.com.marketplace.api.domain.view.pagination.Page;
import onlydust.com.marketplace.api.domain.view.pagination.PaginationHelper;

import java.util.List;

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


    static ContributionPageResponse mapContributionPageResponse(Integer pageIndex,
                                                                List<Project> contributedProjects,
                                                                List<GithubRepo> contributedRepos,
                                                                Page<ContributionView> contributions) {

        return new ContributionPageResponse()
                .projects(contributedProjects.stream().map(ProjectMapper::mapShortProjectResponse).toList())
                .repos(contributedRepos.stream().map(GithubRepoMapper::mapRepoToShortResponse).toList())
                .contributions(contributions.getContent().stream().map(ContributionMapper::mapContribution).toList())
                .hasMore(hasMore(pageIndex, contributions.getTotalPageNumber()))
                .totalPageNumber(contributions.getTotalPageNumber())
                .totalItemNumber(contributions.getTotalItemNumber())
                .nextPageIndex(PaginationHelper.nextPageIndex(pageIndex, contributions.getTotalPageNumber()));
    }

    static ContributionPageItemResponse mapContribution(ContributionView contributionView) {
        return new ContributionPageItemResponse()
                .id(contributionView.getId())
                .createdAt(DateMapper.toZoneDateTime(contributionView.getCreatedAt()))
                .completedAt(DateMapper.toZoneDateTime(contributionView.getCompletedAt()))
                .type(mapContributionTypeToResponse(contributionView.getType()))
                .status(ContributionMapper.mapContributionStatusToResponse(contributionView.getStatus()))
                .githubNumber(contributionView.getGithubNumber())
                .githubTitle(contributionView.getGithubTitle())
                .githubHtmlUrl(contributionView.getGithubHtmlUrl())
                .githubBody(contributionView.getGithubBody())
                .projectName(contributionView.getProjectName())
                .repoName(contributionView.getRepoName())
                .links(contributionView.getLinks().stream().map(ContributionMapper::mapContributionLink).toList())
                .rewardIds(contributionView.getRewardIds());
    }

    static ContributionLinkResponse mapContributionLink(ContributionLinkView link) {
        return new ContributionLinkResponse()
                .id(link.getId())
                .createdAt(DateMapper.toZoneDateTime(link.getCreatedAt()))
                .completedAt(DateMapper.toZoneDateTime(link.getCompletedAt()))
                .type(mapContributionTypeToResponse(link.getType()))
                .status(ContributionMapper.mapContributionStatusToResponse(link.getStatus()))
                .githubNumber(link.getGithubNumber())
                .githubTitle(link.getGithubTitle())
                .githubHtmlUrl(link.getGithubHtmlUrl())
                .githubBody(link.getGithubBody())
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
        };
    }

    static ContributionDetailsResponse mapContributionDetails(ContributionDetailsView contribution) {
        return new ContributionDetailsResponse()
                .id(contribution.getId())
                .createdAt(DateMapper.toZoneDateTime(contribution.getCreatedAt()))
                .completedAt(DateMapper.toZoneDateTime(contribution.getCompletedAt()))
                .type(mapContributionTypeToResponse(contribution.getType()))
                .status(ContributionMapper.mapContributionStatusToResponse(contribution.getStatus()))
                .githubNumber(contribution.getGithubNumber())
                .githubTitle(contribution.getGithubTitle())
                .githubHtmlUrl(contribution.getGithubHtmlUrl())
                .githubBody(contribution.getGithubBody())
                .projectName(contribution.getProjectName())
                .repoName(contribution.getRepoName())
                .links(contribution.getLinks().stream().map(ContributionMapper::mapContributionLink).toList())
                .rewards(contribution.getRewards().stream().map(RewardMapper::rewardToResponse).toList());
    }
}
