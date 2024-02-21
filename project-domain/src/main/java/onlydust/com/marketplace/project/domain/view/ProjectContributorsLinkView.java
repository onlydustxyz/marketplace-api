package onlydust.com.marketplace.project.domain.view;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProjectContributorsLinkView implements UserLinkView {

    Long githubUserId;
    String login;
    String avatarUrl;
    String url;
    TotalsEarned earned;
    Integer contributionCount;
    Integer rewards;
    Integer totalToReward;
    Integer pullRequestsToRewardCount;
    Integer issuesToRewardCount;
    Integer codeReviewToRewardCount;
    Boolean isRegistered;
    Boolean isHidden;

    public enum SortBy {
        contributionCount, earned, login, rewardCount, toRewardCount
    }


}
