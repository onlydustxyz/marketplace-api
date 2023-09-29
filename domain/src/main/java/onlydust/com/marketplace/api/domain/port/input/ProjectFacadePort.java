package onlydust.com.marketplace.api.domain.port.input;

import onlydust.com.marketplace.api.domain.model.Project;
import onlydust.com.marketplace.api.domain.view.Page;
import onlydust.com.marketplace.api.domain.view.ProjectView;

import java.util.List;
import java.util.UUID;

public interface ProjectFacadePort {
    Project getById(UUID projectId);

    Project getBySlug(String slug);

    Page<ProjectView> getByTechnologiesSponsorsOwnershipSearchSortBy(List<String> technology, List<String> sponsor,
                                                                     String ownership, String search, String sort);
}
