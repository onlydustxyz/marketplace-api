package onlydust.com.marketplace.project.domain.service;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import onlydust.com.marketplace.kernel.model.ProjectId;
import onlydust.com.marketplace.project.domain.model.ProjectContributorLabel;
import onlydust.com.marketplace.project.domain.port.input.ProjectContributorLabelFacadePort;
import onlydust.com.marketplace.project.domain.port.output.ProjectContributorLabelStoragePort;

@AllArgsConstructor
public class ProjectContributorLabelService implements ProjectContributorLabelFacadePort {
    private final ProjectContributorLabelStoragePort projectContributorLabelStoragePort;

    @Override
    public ProjectContributorLabel createContributorLabel(@NonNull ProjectId projectId, @NonNull String name) {
        final var projectContributorLabel = ProjectContributorLabel.of(projectId, name);
        projectContributorLabelStoragePort.save(projectContributorLabel);
        return projectContributorLabel;
    }
}
