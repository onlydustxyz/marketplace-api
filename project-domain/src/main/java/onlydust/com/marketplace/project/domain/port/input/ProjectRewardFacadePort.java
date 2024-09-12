package onlydust.com.marketplace.project.domain.port.input;

import onlydust.com.marketplace.kernel.model.ProjectId;
import onlydust.com.marketplace.kernel.model.UserId;
import onlydust.com.marketplace.kernel.pagination.Page;
import onlydust.com.marketplace.project.domain.view.ProjectBudgetsView;
import onlydust.com.marketplace.project.domain.view.RewardDetailsView;
import onlydust.com.marketplace.project.domain.view.RewardItemView;

import java.util.UUID;

public interface ProjectRewardFacadePort {
    ProjectBudgetsView getBudgets(ProjectId projectId, UserId projectLeadId);

    RewardDetailsView getRewardByIdForProjectLead(ProjectId projectId, UUID rewardId, UserId userId);

    Page<RewardItemView> getRewardItemsPageByIdForProjectLead(ProjectId projectId, UUID rewardId, UserId projectLead,
                                                              int pageIndex, int pageSize);
}
