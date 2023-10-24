package onlydust.com.marketplace.api.domain.view;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class ProjectContributorsLinkView implements UserLinkView {

    Long githubUserId;
    String login;
    String avatarUrl;
    String url;
    BigDecimal earned;
    Integer contributionCount;
    Integer rewards;
    Integer totalToReward;
    Integer pullRequestsToRewardCount;
    Integer issuesToRewardCount;
    Integer codeReviewToRewardCount;
    Boolean isRegistered;

    public enum SortBy {
        contributionCount, earned, login, rewardCount, toRewardCount
    }

}
