package onlydust.com.marketplace.project.domain.port.input;

import onlydust.com.marketplace.kernel.pagination.Page;
import onlydust.com.marketplace.project.domain.view.ProjectBudgetsView;
import onlydust.com.marketplace.project.domain.view.RewardDetailsView;
import onlydust.com.marketplace.project.domain.view.RewardItemView;

import java.util.UUID;

public interface ProjectRewardFacadePort {
    ProjectBudgetsView getBudgets(UUID projectId, UUID projectLeadId);

    RewardDetailsView getRewardByIdForProjectLead(UUID projectId, UUID rewardId, UUID userId);

    Page<RewardItemView> getRewardItemsPageByIdForProjectLead(UUID projectId, UUID rewardId, UUID projectLead,
                                                              int pageIndex, int pageSize);
}
