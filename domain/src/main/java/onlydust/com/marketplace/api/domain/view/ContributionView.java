package onlydust.com.marketplace.api.domain.view;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Value;
import onlydust.com.marketplace.api.domain.model.ContributionStatus;
import onlydust.com.marketplace.api.domain.model.ContributionType;
import onlydust.com.marketplace.api.domain.model.GithubRepo;
import onlydust.com.marketplace.api.domain.model.Project;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Value
@Builder
@EqualsAndHashCode(callSuper = true)
public class ContributionView extends ContributionBaseView {
    String id;
    Date createdAt;
    Date completedAt;
    ContributionType type;
    ContributionStatus status;
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
    List<CodeReviewState> codeReviewStates;

    public enum Sort {
        CREATED_AT, PROJECT_REPO_NAME, GITHUB_NUMBER_TITLE
    }

    @Value
    @Builder
    public static class Filters {
        @Builder.Default
        List<UUID> projects = List.of();
        @Builder.Default
        List<Long> repos = List.of();
        @Builder.Default
        List<ContributionType> types = List.of();
        @Builder.Default
        List<ContributionStatus> statuses = List.of();
    }
}
