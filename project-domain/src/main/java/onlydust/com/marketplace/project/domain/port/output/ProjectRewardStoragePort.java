package onlydust.com.marketplace.project.domain.port.output;

import onlydust.com.marketplace.kernel.pagination.Page;
import onlydust.com.marketplace.kernel.pagination.SortDirection;
import onlydust.com.marketplace.project.domain.model.Reward;
import onlydust.com.marketplace.project.domain.view.*;

import java.util.UUID;

public interface ProjectRewardStoragePort {

    ProjectRewardsPageView findRewards(UUID projectId, ProjectRewardView.Filters filters,
                                       Reward.SortBy sortBy, SortDirection sortDirection,
                                       int pageIndex, int pageSize);

    ProjectBudgetsView findBudgets(UUID projectId);

    RewardDetailsView getProjectReward(UUID rewardId);

    Page<RewardItemView> getProjectRewardItems(UUID rewardId, int pageIndex, int pageSize);

    void updateUsdAmount(UUID rewardId);
}
