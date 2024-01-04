package onlydust.com.marketplace.api.domain.view;

import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.Data;

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
