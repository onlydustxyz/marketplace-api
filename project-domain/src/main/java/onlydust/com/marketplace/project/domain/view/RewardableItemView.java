package onlydust.com.marketplace.project.domain.view;

import lombok.Builder;
import lombok.Data;
import onlydust.com.marketplace.project.domain.model.ContributionType;

import java.util.Date;


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
    String githubBody;
    UserLinkView githubAuthor;
}
