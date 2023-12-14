package onlydust.com.marketplace.api.rest.api.adapter.mapper;

import com.fasterxml.jackson.annotation.JsonProperty;
import onlydust.com.marketplace.api.contract.model.*;
import onlydust.com.marketplace.api.domain.model.Contributor;
import onlydust.com.marketplace.api.domain.view.*;
import onlydust.com.marketplace.api.domain.view.pagination.Page;
import onlydust.com.marketplace.api.domain.view.pagination.PaginationHelper;

import javax.validation.Valid;
import java.net.URI;
import java.net.URL;
import java.util.List;

import static onlydust.com.marketplace.api.domain.view.pagination.PaginationHelper.hasMore;

public interface ContributorMapper {
    static ContributorResponse of(ContributorLinkView contributorLinkView) {
        return new ContributorResponse()
                .githubUserId(contributorLinkView.getGithubUserId())
                .login(contributorLinkView.getLogin())
                .avatarUrl(contributorLinkView.getAvatarUrl())
                .htmlUrl(URI.create(contributorLinkView.getUrl()))
                .isRegistered(contributorLinkView.getIsRegistered());
    }

    static ProjectChurnedContributorsPageResponse mapProjectChurnedContributorsPageResponse(int pageIndex,
                                                                                            Page<ChurnedContributorView> contributors) {
        return new ProjectChurnedContributorsPageResponse()
                .contributors(contributors.getContent().stream().map(ContributorMapper::mapProjectChurnedContributorsPageItemResponse).toList())
                .hasMore(hasMore(pageIndex, contributors.getTotalPageNumber()))
                .totalPageNumber(contributors.getTotalPageNumber())
                .totalItemNumber(contributors.getTotalItemNumber())
                .nextPageIndex(PaginationHelper.nextPageIndex(pageIndex, contributors.getTotalPageNumber()));
    }

    static ProjectChurnedContributorsPageItemResponse mapProjectChurnedContributorsPageItemResponse(ChurnedContributorView contributor) {
        return new ProjectChurnedContributorsPageItemResponse()
                .githubUserId(contributor.getGithubId())
                .login(contributor.getLogin())
                .htmlUrl(contributor.getHtmlUrl() == null ? null : URI.create(contributor.getHtmlUrl()))
                .avatarUrl(contributor.getAvatarUrl())
                .isRegistered(contributor.getIsRegistered())
                .cover(UserMapper.coverToUserProfileResponse(contributor.getCover()))
                .lastContribution(ContributionMapper.mapChurnedContribution(contributor.getLastContribution()))
                ;
    }

    static ProjectNewcomersPageResponse mapProjectNewcomersPageResponse(int pageIndex,
                                                                        Page<NewcomerView> contributors) {
        return new ProjectNewcomersPageResponse()
                .contributors(contributors.getContent().stream().map(ContributorMapper::mapProjectNewcomersPageItemResponse).toList())
                .hasMore(hasMore(pageIndex, contributors.getTotalPageNumber()))
                .totalPageNumber(contributors.getTotalPageNumber())
                .totalItemNumber(contributors.getTotalItemNumber())
                .nextPageIndex(PaginationHelper.nextPageIndex(pageIndex, contributors.getTotalPageNumber()));
    }

    static ProjectNewcomersPageItemResponse mapProjectNewcomersPageItemResponse(NewcomerView contributor) {
        return new ProjectNewcomersPageItemResponse()
                .githubUserId(contributor.getGithubId())
                .login(contributor.getLogin())
                .htmlUrl(contributor.getHtmlUrl() == null ? null : URI.create(contributor.getHtmlUrl()))
                .avatarUrl(contributor.getAvatarUrl())
                .isRegistered(contributor.getIsRegistered())
                .cover(UserMapper.coverToUserProfileResponse(contributor.getCover()))
                .location(contributor.getLocation())
                .bio(contributor.getBio())
                .firstContributedAt(contributor.getFirstContributedAt())
                ;
    }

    static ProjectContributorActivityPageResponse mapProjectContributorActivityPageResponse(int pageIndex,
                                                                                            Page<ContributorActivityView> contributors) {
        return new ProjectContributorActivityPageResponse()
                .contributors(contributors.getContent().stream().map(ContributorMapper::mapProjectContributorActivityPageItemResponse).toList())
                .hasMore(hasMore(pageIndex, contributors.getTotalPageNumber()))
                .totalPageNumber(contributors.getTotalPageNumber())
                .totalItemNumber(contributors.getTotalItemNumber())
                .nextPageIndex(PaginationHelper.nextPageIndex(pageIndex, contributors.getTotalPageNumber()));
    }

    static ProjectContributorActivityPageItemResponse mapProjectContributorActivityPageItemResponse(ContributorActivityView activity) {
        return new ProjectContributorActivityPageItemResponse()
                .githubUserId(activity.getGithubId())
                .login(activity.getLogin())
                .htmlUrl(activity.getHtmlUrl() == null ? null : URI.create(activity.getHtmlUrl()))
                .avatarUrl(activity.getAvatarUrl())
                .isRegistered(activity.getIsRegistered())
                .completedPullRequestCount(activity.getCompletedPullRequestCount())
                .completedIssueCount(activity.getCompletedIssueCount())
                .completedCodeReviewCount(activity.getCompletedCodeReviewCount())
                .contributionCountPerWeeks(activity.getContributionStats().stream().map(UserMapper::mapContributionStat).toList())
                ;
    }
}
