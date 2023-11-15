package onlydust.com.marketplace.api.domain.view;

import lombok.Builder;
import lombok.Data;
import onlydust.com.marketplace.api.domain.model.ContributionType;

import java.util.Date;


@Builder
@Data
public class RewardableItemView {
    String id;
    ContributionType type;
    RewardItemStatus status;
    Date createdAt;
    Date lastUpdateAt;
    Integer commentsCount;
    Integer commitsCount;
    Integer userCommitsCount;
    Long number;
    String repoName;
    String githubUrl;
    String title;
    CodeReviewOutcome outcome;
}
