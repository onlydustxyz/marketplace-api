package onlydust.com.marketplace.api.domain.port.input;

import onlydust.com.marketplace.api.domain.model.CreateProjectCommand;
import onlydust.com.marketplace.api.domain.view.ProjectCardView;
import onlydust.com.marketplace.api.domain.view.ProjectContributorsLinkView;
import onlydust.com.marketplace.api.domain.view.ProjectDetailsView;
import onlydust.com.marketplace.api.domain.view.ProjectRewardView;
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

    URL saveLogoImage(InputStream imageInputStream);

    Page<ProjectContributorsLinkView> getContributors(UUID projectId, ProjectContributorsLinkView.SortBy sortBy,
                                                      Integer pageIndex,
                                                      Integer pageSize);

    Page<ProjectContributorsLinkView> getContributorsForProjectLeadId(UUID projectId,
                                                                      ProjectContributorsLinkView.SortBy sortBy,
                                                                      UUID projectLeadId, Integer pageIndex,
                                                                      Integer pageSize);

    Page<ProjectRewardView> getRewards(UUID projectId, UUID projectLeadId, Integer pageIndex, Integer pageSize,
                                       ProjectRewardView.SortBy sortBy, SortDirection sortDirection);
}
