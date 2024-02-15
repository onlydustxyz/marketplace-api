package onlydust.com.marketplace.api.domain.view;

import lombok.Builder;
import lombok.Data;
import onlydust.com.marketplace.kernel.pagination.Page;

@Data
@Builder
public class ProjectRewardsPageView {
    Page<ProjectRewardView> rewards;
    Money remainingBudget;
    Money spentAmount;
    Integer sentRewardsCount;
    Integer rewardedContributionsCount;
    Integer rewardedContributorsCount;
}
