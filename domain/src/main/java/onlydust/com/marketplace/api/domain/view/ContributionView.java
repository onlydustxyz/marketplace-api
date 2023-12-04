package onlydust.com.marketplace.api.domain.view;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Value;
import onlydust.com.marketplace.api.domain.model.ContributionStatus;
import onlydust.com.marketplace.api.domain.model.ContributionType;
import onlydust.com.marketplace.api.domain.model.GithubRepo;
import onlydust.com.marketplace.api.domain.model.Project;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Value
@Builder
@EqualsAndHashCode
public class ContributionView {
    String id;
    Date createdAt;
    Date completedAt;
    ContributionType type;
    ContributionStatus status;
    ContributorLinkView contributor;
    Long githubNumber;
    String githubStatus;
    String githubTitle;
    String githubHtmlUrl;
    String githubBody;
    UserLinkView githubAuthor;
    GithubRepo githubRepo;
    Project project;
    List<ContributionLinkView> links;
    List<UUID> rewardIds;
    PullRequestReviewState prReviewState;

    public enum Sort {
        CREATED_AT, PROJECT_REPO_NAME, GITHUB_NUMBER_TITLE, CONTRIBUTOR_LOGIN, LINKS_COUNT
    }

    @Value
    @Builder
    public static class Filters {
        @Builder.Default
        List<UUID> projects = List.of();
        @Builder.Default
        List<Long> contributors = List.of();
        @Builder.Default
        List<Long> repos = List.of();
        @Builder.Default
        List<ContributionType> types = List.of();
        @Builder.Default
        List<ContributionStatus> statuses = List.of();
        Date from;
        Date to;
    }
}
