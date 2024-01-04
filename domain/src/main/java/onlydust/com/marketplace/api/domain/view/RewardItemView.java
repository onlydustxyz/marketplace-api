package onlydust.com.marketplace.api.domain.view;

import java.util.Date;
import lombok.Builder;
import lombok.Data;
import onlydust.com.marketplace.api.domain.model.ContributionType;


@Builder
@Data
public class RewardItemView {

  Long number;
  String id;
  String contributionId;
  String title;
  String githubUrl;
  Date createdAt;
  Date completedAt;
  String repoName;
  ContributionType type;
  RewardItemStatus status;
  Integer commitsCount;
  Integer userCommitsCount;
  Integer commentsCount;
  Long githubAuthorId;
  String authorLogin;
  String authorAvatarUrl;
  String authorGithubUrl;
  Long recipientId;
}
