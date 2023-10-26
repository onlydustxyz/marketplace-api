package onlydust.com.marketplace.api.domain.service;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.domain.exception.OnlyDustException;
import onlydust.com.marketplace.api.domain.model.CreateProjectCommand;
import onlydust.com.marketplace.api.domain.model.ProjectVisibility;
import onlydust.com.marketplace.api.domain.port.input.ProjectFacadePort;
import onlydust.com.marketplace.api.domain.port.output.ImageStoragePort;
import onlydust.com.marketplace.api.domain.port.output.ProjectStoragePort;
import onlydust.com.marketplace.api.domain.port.output.UUIDGeneratorPort;
import onlydust.com.marketplace.api.domain.view.*;
import onlydust.com.marketplace.api.domain.view.pagination.Page;
import onlydust.com.marketplace.api.domain.view.pagination.SortDirection;

import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.UUID;

@AllArgsConstructor
public class ProjectService implements ProjectFacadePort {

    private final ProjectStoragePort projectStoragePort;
    private final ImageStoragePort imageStoragePort;
    private final UUIDGeneratorPort uuidGeneratorPort;
    private final PermissionService permissionService;

    @Override
    public ProjectDetailsView getById(UUID projectId) {
        return projectStoragePort.getById(projectId);
    }

    @Override
    public ProjectDetailsView getBySlug(String slug) {
        return projectStoragePort.getBySlug(slug);
    }


    @Override
    public Page<ProjectCardView> getByTechnologiesSponsorsUserIdSearchSortBy(List<String> technologies,
                                                                             List<String> sponsors, String search,
                                                                             ProjectCardView.SortBy sort, UUID userId
            , Boolean mine) {
        return projectStoragePort.findByTechnologiesSponsorsUserIdSearchSortBy(technologies, sponsors, userId, search,
                sort, mine);
    }

    @Override
    public Page<ProjectCardView> getByTechnologiesSponsorsSearchSortBy(List<String> technologies, List<String> sponsors,
                                                                       String search, ProjectCardView.SortBy sort) {
        return projectStoragePort.findByTechnologiesSponsorsSearchSortBy(technologies, sponsors, search, sort);
    }

    @Override
    public UUID createProject(CreateProjectCommand command) {
        final UUID projectId = uuidGeneratorPort.generate();
        this.projectStoragePort.createProject(projectId, command.getName(),
                command.getShortDescription(), command.getLongDescription(),
                command.getIsLookingForContributors(), command.getMoreInfos(),
                command.getGithubRepoIds(), command.getGithubUserIdsAsProjectLeads(),
                ProjectVisibility.PUBLIC,
                command.getImageUrl());
        return projectId;
    }

    @Override
    public URL saveLogoImage(InputStream imageInputStream) {
        return this.imageStoragePort.storeImage(imageInputStream);
    }

    @Override
    public Page<ProjectContributorsLinkView> getContributors(UUID projectId,
                                                             ProjectContributorsLinkView.SortBy sortBy,
                                                             SortDirection sortDirection,
                                                             Integer pageIndex, Integer pageSize) {
        return projectStoragePort.findContributors(projectId, sortBy, sortDirection, pageIndex, pageSize);
    }

    @Override
    public Page<ProjectContributorsLinkView> getContributorsForProjectLeadId(UUID projectId,
                                                                             ProjectContributorsLinkView.SortBy sortBy,
                                                                             SortDirection sortDirection,
                                                                             UUID projectLeadId, Integer pageIndex,
                                                                             Integer pageSize) {
        if (permissionService.isUserProjectLead(projectId, projectLeadId)) {
            return projectStoragePort.findContributorsForProjectLead(projectId, sortBy, sortDirection, pageIndex,
                    pageSize);
        } else {
            return projectStoragePort.findContributors(projectId, sortBy, sortDirection, pageIndex, pageSize);
        }
    }

    @Override
    public Page<ProjectRewardView> getRewards(UUID projectId, UUID projectLeadId, Integer pageIndex, Integer pageSize
            , ProjectRewardView.SortBy sortBy, SortDirection sortDirection) {
        if (permissionService.isUserProjectLead(projectId, projectLeadId)) {
            return projectStoragePort.findRewards(projectId, sortBy, sortDirection, pageIndex, pageSize);
        } else {
            throw OnlyDustException.forbidden("Only project leads can read rewards on their projects");
        }
    }

    @Override
    public ProjectBudgetsView getBudgets(UUID projectId, UUID projectLeadId) {
        if (permissionService.isUserProjectLead(projectId, projectLeadId)) {
            return projectStoragePort.findBudgets(projectId);
        } else {
            throw OnlyDustException.forbidden("Only project leads can read budgets on their projects");
        }
    }

    @Override
    public RewardView getRewardByIdForProjectLead(UUID projectId, UUID rewardId, UUID projectLeadId) {
        if (permissionService.isUserProjectLead(projectId, projectLeadId)) {
            return projectStoragePort.getProjectReward(rewardId);
        } else {
            throw OnlyDustException.forbidden("Only project leads can read reward on their projects");
        }
    }
}
