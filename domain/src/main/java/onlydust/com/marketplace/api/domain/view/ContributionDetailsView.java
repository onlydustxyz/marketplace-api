package onlydust.com.marketplace.api.domain.view;

import java.util.Date;
import java.util.List;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Value;
import onlydust.com.marketplace.api.domain.model.ContributionStatus;
import onlydust.com.marketplace.api.domain.model.ContributionType;
import onlydust.com.marketplace.api.domain.model.GithubRepo;
import onlydust.com.marketplace.api.domain.model.Project;

@Value
@Builder(toBuilder = true)
@EqualsAndHashCode
public class ContributionDetailsView {

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
  Integer githubCommentsCount;
  Integer githubCommitsCount;
  Integer githubUserCommitsCount;
  UserLinkView githubAuthor;
  GithubRepo githubRepo;
  Project project;
  List<ContributionLinkView> links;
  List<ContributionRewardView> rewards;
  PullRequestReviewState prReviewState;

  public ContributionDetailsView withRewards(List<ContributionRewardView> rewards) {
    return this.toBuilder()
        .rewards(rewards)
        .build();
  }
}
