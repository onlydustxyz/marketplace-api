package onlydust.com.marketplace.api.domain.port.input;

import onlydust.com.marketplace.api.domain.model.CreateProjectCommand;
import onlydust.com.marketplace.api.domain.model.UpdateProjectCommand;
import onlydust.com.marketplace.api.domain.view.*;
import onlydust.com.marketplace.api.domain.view.pagination.Page;
import onlydust.com.marketplace.api.domain.view.pagination.SortDirection;

import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.UUID;

public interface ProjectFacadePort {
    ProjectDetailsView getById(UUID projectId);

    ProjectDetailsView getBySlug(String slug);

    Page<ProjectCardView> getByTechnologiesSponsorsUserIdSearchSortBy(List<String> technologies, List<String> sponsors,
                                                                      String search, ProjectCardView.SortBy sort,
                                                                      UUID userId, Boolean mine);

    Page<ProjectCardView> getByTechnologiesSponsorsSearchSortBy(List<String> technologies, List<String> sponsors,
                                                                String search, ProjectCardView.SortBy sort);


    UUID createProject(CreateProjectCommand createProjectCommand);

    void updateProject(UpdateProjectCommand updateProjectCommand);

    URL saveLogoImage(InputStream imageInputStream);

    Page<ProjectContributorsLinkView> getContributors(UUID projectId, ProjectContributorsLinkView.SortBy sortBy,
                                                      SortDirection sortDirection,
                                                      Integer pageIndex,
                                                      Integer pageSize);

    Page<ProjectContributorsLinkView> getContributorsForProjectLeadId(UUID projectId,
                                                                      ProjectContributorsLinkView.SortBy sortBy,
                                                                      SortDirection sortDirection,
                                                                      UUID projectLeadId, Integer pageIndex,
                                                                      Integer pageSize);

    Page<ProjectRewardView> getRewards(UUID projectId, UUID projectLeadId, Integer pageIndex, Integer pageSize,
                                       ProjectRewardView.SortBy sortBy, SortDirection sortDirection);

    ProjectBudgetsView getBudgets(UUID projectId, UUID projectLeadId);

    RewardView getRewardByIdForProjectLead(UUID projectId, UUID rewardId, UUID userId);

    Page<RewardItemView> getRewardItemsPageByIdForProjectLead(UUID projectId, UUID rewardId, UUID projectLead,
                                                              int pageIndex, int pageSize);
}
