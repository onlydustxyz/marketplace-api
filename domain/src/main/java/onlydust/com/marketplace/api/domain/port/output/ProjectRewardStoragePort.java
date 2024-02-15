package onlydust.com.marketplace.api.domain.port.output;

import onlydust.com.marketplace.api.domain.view.*;
import onlydust.com.marketplace.kernel.pagination.Page;
import onlydust.com.marketplace.kernel.pagination.SortDirection;

import java.util.UUID;

public interface ProjectRewardStoragePort {

    ProjectRewardsPageView findRewards(UUID projectId, ProjectRewardView.Filters filters,
                                       ProjectRewardView.SortBy sortBy, SortDirection sortDirection,
                                       int pageIndex, int pageSize);

    ProjectBudgetsView findBudgets(UUID projectId);

    RewardView getProjectReward(UUID rewardId);

    Page<RewardItemView> getProjectRewardItems(UUID rewardId, int pageIndex, int pageSize);

}
