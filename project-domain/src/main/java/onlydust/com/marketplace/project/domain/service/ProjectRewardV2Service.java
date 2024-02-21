package onlydust.com.marketplace.project.domain.service;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.kernel.pagination.Page;
import onlydust.com.marketplace.kernel.pagination.SortDirection;
import onlydust.com.marketplace.project.domain.port.input.ProjectRewardFacadePort;
import onlydust.com.marketplace.project.domain.port.output.ProjectRewardStoragePort;
import onlydust.com.marketplace.project.domain.view.*;

import java.util.UUID;

import static onlydust.com.marketplace.kernel.exception.OnlyDustException.forbidden;

@AllArgsConstructor
public class ProjectRewardV2Service implements ProjectRewardFacadePort {
    private final ProjectRewardStoragePort projectRewardStoragePortV2;
    private final PermissionService permissionService;

    @Override
    public ProjectRewardsPageView getRewards(UUID projectId,
                                             UUID projectLeadId,
                                             ProjectRewardView.Filters filters, Integer pageIndex, Integer pageSize,
                                             ProjectRewardView.SortBy sortBy, SortDirection sortDirection) {
        if (permissionService.isUserProjectLead(projectId, projectLeadId)) {
            return projectRewardStoragePortV2.findRewards(projectId, filters, sortBy, sortDirection, pageIndex, pageSize);
        } else {
            throw forbidden("Only project leads can read rewards on their projects");
        }
    }

    @Override
    public ProjectBudgetsView getBudgets(UUID projectId, UUID projectLeadId) {
        if (permissionService.isUserProjectLead(projectId, projectLeadId)) {
            return projectRewardStoragePortV2.findBudgets(projectId);
        } else {
            throw forbidden("Only project leads can read budgets on their projects");
        }
    }

    @Override
    public RewardView getRewardByIdForProjectLead(UUID projectId, UUID rewardId, UUID projectLeadId) {
        if (permissionService.isUserProjectLead(projectId, projectLeadId)) {
            return projectRewardStoragePortV2.getProjectReward(rewardId);
        } else {
            throw forbidden("Only project leads can read reward on their projects");
        }
    }

    @Override
    public Page<RewardItemView> getRewardItemsPageByIdForProjectLead(UUID projectId, UUID rewardId,
                                                                     UUID projectLeadId, int pageIndex, int pageSize) {
        if (permissionService.isUserProjectLead(projectId, projectLeadId)) {
            return projectRewardStoragePortV2.getProjectRewardItems(rewardId, pageIndex, pageSize);
        } else {
            throw forbidden("Only project leads can read reward items on their projects");
        }
    }
}
