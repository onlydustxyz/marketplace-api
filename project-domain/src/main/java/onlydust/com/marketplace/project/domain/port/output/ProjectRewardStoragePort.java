package onlydust.com.marketplace.project.domain.port.output;

import onlydust.com.marketplace.kernel.model.ProjectId;
import onlydust.com.marketplace.kernel.pagination.Page;
import onlydust.com.marketplace.project.domain.view.ProjectBudgetsView;
import onlydust.com.marketplace.project.domain.view.RewardDetailsView;
import onlydust.com.marketplace.project.domain.view.RewardItemView;

import java.util.UUID;

public interface ProjectRewardStoragePort {
    ProjectBudgetsView findBudgets(ProjectId projectId);

    RewardDetailsView getProjectReward(UUID rewardId);

    Page<RewardItemView> getProjectRewardItems(UUID rewardId, int pageIndex, int pageSize);
}
