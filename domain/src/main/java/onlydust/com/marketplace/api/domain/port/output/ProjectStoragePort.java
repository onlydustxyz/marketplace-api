package onlydust.com.marketplace.api.domain.port.output;

import onlydust.com.marketplace.api.domain.model.Contributor;
import onlydust.com.marketplace.api.domain.model.CreateProjectCommand;
import onlydust.com.marketplace.api.domain.model.ProjectVisibility;
import onlydust.com.marketplace.api.domain.view.Page;
import onlydust.com.marketplace.api.domain.view.ProjectCardView;
import onlydust.com.marketplace.api.domain.view.ProjectDetailsView;

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
}
