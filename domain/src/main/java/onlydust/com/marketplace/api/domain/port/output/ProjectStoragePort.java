package onlydust.com.marketplace.api.domain.port.output;

import onlydust.com.marketplace.api.domain.model.Contributor;
import onlydust.com.marketplace.api.domain.model.CreateProjectCommand;
import onlydust.com.marketplace.api.domain.model.ProjectVisibility;
import onlydust.com.marketplace.api.domain.view.ProjectCardView;
import onlydust.com.marketplace.api.domain.view.ProjectContributorsLinkView;
import onlydust.com.marketplace.api.domain.view.ProjectDetailsView;
import onlydust.com.marketplace.api.domain.view.ProjectRewardView;
import onlydust.com.marketplace.api.domain.view.pagination.Page;
import onlydust.com.marketplace.api.domain.view.pagination.SortDirection;

import java.util.List;
import java.util.UUID;

public interface ProjectStoragePort {
    ProjectDetailsView getById(UUID projectId);

    ProjectDetailsView getBySlug(String slug);

    Page<ProjectCardView> findByTechnologiesSponsorsUserIdSearchSortBy(List<String> technologies, List<String> sponsors,
                                                                       UUID userId, String search,
                                                                       ProjectCardView.SortBy sort, Boolean mine);

    Page<ProjectCardView> findByTechnologiesSponsorsSearchSortBy(List<String> technologies, List<String> sponsors,
                                                                 String search, ProjectCardView.SortBy sort);

    void createProject(UUID projectId, String name, String shortDescription, String longDescription,
                       Boolean isLookingForContributors, List<CreateProjectCommand.MoreInfo> moreInfos,
                       List<Long> githubRepoIds, List<Long> githubUserIdsAsProjectLeads, ProjectVisibility visibility
            , String imageUrl);

    List<Contributor> searchContributorsByLogin(UUID projectId, String login);

    Page<ProjectContributorsLinkView> findContributors(UUID projectId, ProjectContributorsLinkView.SortBy sortBy,
                                                       int pageIndex, int pageSize);

    Page<ProjectContributorsLinkView> findContributorsForProjectLead(UUID projectId,
                                                                     ProjectContributorsLinkView.SortBy sortBy,
                                                                     int pageIndex, int pageSize);

    List<UUID> getProjectLeadIds(UUID projectId);

    Page<ProjectRewardView> findRewards(UUID projectId, ProjectRewardView.SortBy sortBy, SortDirection sortDirection,
                                        int pageIndex, int pageSize);
}
