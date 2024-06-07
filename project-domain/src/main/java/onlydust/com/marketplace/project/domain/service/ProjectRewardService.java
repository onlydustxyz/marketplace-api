package onlydust.com.marketplace.project.domain.service;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.kernel.pagination.Page;
import onlydust.com.marketplace.project.domain.port.input.ProjectRewardFacadePort;
import onlydust.com.marketplace.project.domain.port.output.ProjectRewardStoragePort;
import onlydust.com.marketplace.project.domain.view.ProjectBudgetsView;
import onlydust.com.marketplace.project.domain.view.RewardDetailsView;
import onlydust.com.marketplace.project.domain.view.RewardItemView;

import java.util.UUID;

import static onlydust.com.marketplace.kernel.exception.OnlyDustException.forbidden;

@AllArgsConstructor
public class ProjectRewardService implements ProjectRewardFacadePort {
    private final ProjectRewardStoragePort projectRewardStoragePort;
    private final PermissionService permissionService;

    @Override
    public ProjectBudgetsView getBudgets(UUID projectId, UUID projectLeadId) {
        if (permissionService.isUserProjectLead(projectId, projectLeadId)) {
            return projectRewardStoragePort.findBudgets(projectId);
        } else {
            throw forbidden("Only project leads can read budgets on their projects");
        }
    }

    @Override
    public RewardDetailsView getRewardByIdForProjectLead(UUID projectId, UUID rewardId, UUID projectLeadId) {
        if (permissionService.isUserProjectLead(projectId, projectLeadId)) {
            return projectRewardStoragePort.getProjectReward(rewardId);
        } else {
            throw forbidden("Only project leads can read reward on their projects");
        }
    }

    @Override
    public Page<RewardItemView> getRewardItemsPageByIdForProjectLead(UUID projectId, UUID rewardId,
                                                                     UUID projectLeadId, int pageIndex, int pageSize) {
        if (permissionService.isUserProjectLead(projectId, projectLeadId)) {
            return projectRewardStoragePort.getProjectRewardItems(rewardId, pageIndex, pageSize);
        } else {
            throw forbidden("Only project leads can read reward items on their projects");
        }
    }
}
