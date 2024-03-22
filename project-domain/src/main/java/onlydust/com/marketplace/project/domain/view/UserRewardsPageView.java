package onlydust.com.marketplace.project.domain.view;

import lombok.Builder;
import lombok.Data;
import onlydust.com.marketplace.kernel.pagination.Page;

@Data
@Builder
public class UserRewardsPageView {
    Page<UserRewardView> rewards;
    Money rewardedAmount;
    Money pendingAmount;
    Integer receivedRewardsCount;
    Integer rewardedContributionsCount;
    Integer rewardingProjectsCount;
    Integer pendingRequestCount;
}
