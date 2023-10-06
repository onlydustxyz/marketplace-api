package onlydust.com.marketplace.api.domain.port.output;

import onlydust.com.marketplace.api.domain.view.Page;
import onlydust.com.marketplace.api.domain.view.ProjectCardView;
import onlydust.com.marketplace.api.domain.view.ProjectDetailsView;

import java.util.List;
import java.util.UUID;

public interface ProjectStoragePort {
    ProjectDetailsView getById(UUID projectId);

    ProjectDetailsView getBySlug(String slug);

    Page<ProjectCardView> findByTechnologiesSponsorsUserIdSearchSortBy(List<String> technology, List<String> sponsor,
                                                                       UUID userId, String search, ProjectCardView.SortBy sort);
}
