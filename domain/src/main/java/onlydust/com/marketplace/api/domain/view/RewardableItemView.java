package onlydust.com.marketplace.api.domain.view;

import java.util.Date;
import lombok.Builder;
import lombok.Data;
import onlydust.com.marketplace.api.domain.model.ContributionType;


@Builder
@Data
public class RewardableItemView {

  String id;
  String contributionId;
  ContributionType type;
  RewardItemStatus status;
  Date createdAt;
  Date completedAt;
  Integer commentsCount;
  Integer commitsCount;
  Integer userCommitsCount;
  Long number;
  String repoName;
  Long repoId;
  String githubUrl;
  String title;
  Boolean ignored;
}
