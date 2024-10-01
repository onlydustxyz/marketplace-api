package onlydust.com.marketplace.project.domain.service;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import onlydust.com.marketplace.kernel.model.ProjectId;
import onlydust.com.marketplace.kernel.model.UserId;
import onlydust.com.marketplace.project.domain.model.ProjectContributorLabel;
import onlydust.com.marketplace.project.domain.port.input.ProjectContributorLabelFacadePort;
import onlydust.com.marketplace.project.domain.port.output.ProjectContributorLabelStoragePort;

import static onlydust.com.marketplace.kernel.exception.OnlyDustException.forbidden;
import static onlydust.com.marketplace.kernel.exception.OnlyDustException.notFound;

@AllArgsConstructor
public class ProjectContributorLabelService implements ProjectContributorLabelFacadePort {
    private final PermissionService permissionService;
    private final ProjectContributorLabelStoragePort projectContributorLabelStoragePort;

    @Override
    public ProjectContributorLabel createLabel(@NonNull UserId projectLeadId, final @NonNull ProjectId projectId, final @NonNull String name) {
        if (!permissionService.isUserProjectLead(projectId, projectLeadId))
            throw forbidden("Only project leaders can create labels");
        final var projectContributorLabel = ProjectContributorLabel.of(projectId, name);
        projectContributorLabelStoragePort.save(projectContributorLabel);
        return projectContributorLabel;
    }

    @Override
    public void deleteLabel(final @NonNull UserId projectLeadId, final @NonNull ProjectContributorLabel.Id labelId) {
        final var projectContributorLabel = projectContributorLabelStoragePort.get(labelId)
                .orElseThrow(() -> notFound("Label %s not found".formatted(labelId)));
        if (!permissionService.isUserProjectLead(projectContributorLabel.projectId(), projectLeadId))
            throw forbidden("Only project leaders can delete labels");
        projectContributorLabelStoragePort.delete(labelId);
    }

    @Override
    public void updateLabel(final @NonNull UserId projectLeadId, final @NonNull ProjectContributorLabel.Id labelId, final @NonNull String name) {
        final var projectContributorLabel = projectContributorLabelStoragePort.get(labelId)
                .orElseThrow(() -> notFound("Label %s not found".formatted(labelId)));
        if (!permissionService.isUserProjectLead(projectContributorLabel.projectId(), projectLeadId))
            throw forbidden("Only project leaders can update labels");
        projectContributorLabel.name(name);
        projectContributorLabelStoragePort.save(projectContributorLabel);
    }
}
