package onlydust.com.marketplace.api.domain.view;

import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class ContributorView {
    UUID id;
    String avatarUrl;
    String login;
    Integer contributionsCount;
    Integer contributionsToReward;
    BigDecimal earned;
    Integer rewardCount;

    public enum SortBy {
        LOGIN, CONTRIBUTION_COUNT, REWARD_COUNT, EARNED
    }
}
