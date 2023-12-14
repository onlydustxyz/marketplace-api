package onlydust.com.marketplace.api.domain.view;

import lombok.Builder;
import lombok.Data;
import onlydust.com.marketplace.api.domain.model.UserProfileCover;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
public class ContributorActivityView {
    Long githubId;
    String login;
    String htmlUrl;
    String avatarUrl;
    Boolean isRegistered;
    Integer completedPullRequestCount;
    Integer completedIssueCount;
    Integer completedCodeReviewCount;
    @Builder.Default
    List<UserProfileView.ProfileStats.ContributionStats> contributionStats = new ArrayList<>();
}
