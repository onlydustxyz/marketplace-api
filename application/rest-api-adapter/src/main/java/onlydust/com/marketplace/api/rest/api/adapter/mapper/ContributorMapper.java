package onlydust.com.marketplace.api.rest.api.adapter.mapper;

import static onlydust.com.marketplace.api.domain.view.pagination.PaginationHelper.hasMore;

import java.net.URI;
import java.time.ZonedDateTime;
import java.time.temporal.WeekFields;
import java.util.stream.IntStream;
import onlydust.com.marketplace.api.contract.model.ContributorResponse;
import onlydust.com.marketplace.api.contract.model.ProjectChurnedContributorsPageItemResponse;
import onlydust.com.marketplace.api.contract.model.ProjectChurnedContributorsPageResponse;
import onlydust.com.marketplace.api.contract.model.ProjectContributorActivityPageItemResponse;
import onlydust.com.marketplace.api.contract.model.ProjectContributorActivityPageResponse;
import onlydust.com.marketplace.api.contract.model.ProjectNewcomersPageItemResponse;
import onlydust.com.marketplace.api.contract.model.ProjectNewcomersPageResponse;
import onlydust.com.marketplace.api.contract.model.UserContributionStats;
import onlydust.com.marketplace.api.domain.view.ChurnedContributorView;
import onlydust.com.marketplace.api.domain.view.ContributorActivityView;
import onlydust.com.marketplace.api.domain.view.ContributorLinkView;
import onlydust.com.marketplace.api.domain.view.NewcomerView;
import onlydust.com.marketplace.api.domain.view.UserProfileView.ProfileStats.ContributionStats;
import onlydust.com.marketplace.api.domain.view.pagination.Page;
import onlydust.com.marketplace.api.domain.view.pagination.PaginationHelper;

public interface ContributorMapper {

  int CONTRIBUTIONS_STATS_WEEK_COUNT = 10;

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
    final var counts = IntStream.range(0, CONTRIBUTIONS_STATS_WEEK_COUNT)
        .mapToObj(w -> ZonedDateTime.now().minusWeeks(w))
        .map(date -> {
              final var year = date.getYear();
              final var weekNumber = date.get(WeekFields.ISO.weekOfYear());
              final var stats = activity.getContributionStats().stream()
                  .filter(contributionStat -> contributionStat.getWeek() == weekNumber && contributionStat.getYear() == year)
                  .findFirst();

              return new UserContributionStats()
                  .year(year)
                  .week(weekNumber)
                  .pullRequestCount(stats.map(ContributionStats::getPullRequestCount).orElse(0))
                  .issueCount(stats.map(ContributionStats::getIssueCount).orElse(0))
                  .codeReviewCount(stats.map(ContributionStats::getCodeReviewCount).orElse(0));
            }
        )
        .toList();

    return new ProjectContributorActivityPageItemResponse()
        .githubUserId(activity.getGithubId())
        .login(activity.getLogin())
        .htmlUrl(activity.getHtmlUrl() == null ? null : URI.create(activity.getHtmlUrl()))
        .avatarUrl(activity.getAvatarUrl())
        .isRegistered(activity.getIsRegistered())
        .completedPullRequestCount(activity.getCompletedPullRequestCount())
        .completedIssueCount(activity.getCompletedIssueCount())
        .completedCodeReviewCount(activity.getCompletedCodeReviewCount())
        .contributionCountPerWeeks(counts)
        ;
  }
}
