package onlydust.com.marketplace.api.domain.service;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.domain.model.Project;
import onlydust.com.marketplace.api.domain.port.input.ProjectFacadePort;
import onlydust.com.marketplace.api.domain.port.output.ProjectStoragePort;

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
}
