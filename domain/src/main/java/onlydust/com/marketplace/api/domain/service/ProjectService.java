package onlydust.com.marketplace.api.domain.service;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.domain.model.Project;
import onlydust.com.marketplace.api.domain.port.input.ProjectFacadePort;
import onlydust.com.marketplace.api.domain.port.output.ProjectStoragePort;
import onlydust.com.marketplace.api.domain.view.Page;
import onlydust.com.marketplace.api.domain.view.ProjectView;

import java.util.List;
import java.util.UUID;

@AllArgsConstructor
public class ProjectService implements ProjectFacadePort {

    private final ProjectStoragePort projectStoragePort;

    @Override
    public Project getById(UUID projectId) {
        return projectStoragePort.getById(projectId);
    }

    @Override
    public Project getBySlug(String slug) {
        return projectStoragePort.getBySlug(slug);
    }

    @Override
    public Page<ProjectView> getByTechnologiesSponsorsOwnershipSearchSortBy(List<String> technology,
                                                                            List<String> sponsor, String ownership,
                                                                            String search, String sort) {
        return projectStoragePort.findByTechnologiesSponsorsOwnershipSearchSortBy(technology, sponsor, ownership,
                search, sort);
    }
}
