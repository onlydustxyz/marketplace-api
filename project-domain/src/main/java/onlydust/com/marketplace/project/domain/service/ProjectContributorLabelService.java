package onlydust.com.marketplace.project.domain.service;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import onlydust.com.marketplace.kernel.model.ProjectId;
import onlydust.com.marketplace.kernel.model.UserId;
import onlydust.com.marketplace.project.domain.model.ProjectContributorLabel;
import onlydust.com.marketplace.project.domain.port.input.ProjectContributorLabelFacadePort;
import onlydust.com.marketplace.project.domain.port.output.ProjectContributorLabelStoragePort;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import static onlydust.com.marketplace.kernel.exception.OnlyDustException.forbidden;
import static onlydust.com.marketplace.kernel.exception.OnlyDustException.notFound;

@AllArgsConstructor
public class ProjectContributorLabelService implements ProjectContributorLabelFacadePort {
    private final PermissionService permissionService;
    private final ProjectContributorLabelStoragePort projectContributorLabelStoragePort;

    @Override
    @Transactional
    public ProjectContributorLabel createLabel(@NonNull UserId projectLeadId, final @NonNull ProjectId projectId, final @NonNull String name) {
        if (!permissionService.isUserProjectLead(projectId, projectLeadId))
            throw forbidden("Only project leaders can create labels");
        final var projectContributorLabel = ProjectContributorLabel.of(projectId, name);
        projectContributorLabelStoragePort.save(projectContributorLabel);
        return projectContributorLabel;
    }

    @Override
    @Transactional
    public void deleteLabel(final @NonNull UserId projectLeadId, final @NonNull ProjectContributorLabel.Id labelId) {
        final var projectContributorLabel = projectContributorLabelStoragePort.get(labelId)
                .orElseThrow(() -> notFound("Label %s not found".formatted(labelId)));
        if (!permissionService.isUserProjectLead(projectContributorLabel.projectId(), projectLeadId))
            throw forbidden("Only project leaders can delete labels");
        projectContributorLabelStoragePort.delete(labelId);
    }

    @Override
    @Transactional
    public void updateLabel(final @NonNull UserId projectLeadId, final @NonNull ProjectContributorLabel.Id labelId, final @NonNull String name) {
        final var projectContributorLabel = projectContributorLabelStoragePort.get(labelId)
                .orElseThrow(() -> notFound("Label %s not found".formatted(labelId)));
        if (!permissionService.isUserProjectLead(projectContributorLabel.projectId(), projectLeadId))
            throw forbidden("Only project leaders can update labels");
        projectContributorLabel.name(name);
        projectContributorLabelStoragePort.save(projectContributorLabel);
    }

    @Override
    @Transactional
    public void updateLabelsOfContributors(final @NonNull UserId projectLeadId, final @NonNull ProjectId projectId,
                                           final @NonNull Map<Long, List<ProjectContributorLabel.Id>> labelsPerContributor) {
        if (!permissionService.isUserProjectLead(projectId, projectLeadId))
            throw forbidden("Only project leaders can assign labels");

        labelsPerContributor.forEach((contributorId, labels) -> {
            projectContributorLabelStoragePort.deleteLabelsOfContributor(projectId, contributorId);
            labels.forEach(labelId -> {
                final var label = projectContributorLabelStoragePort.get(labelId)
                        .orElseThrow(() -> notFound("Label %s not found".formatted(labelId)));
                if (!label.projectId().equals(projectId))
                    throw forbidden("Label %s does not belong to project %s".formatted(labelId, projectId));
                projectContributorLabelStoragePort.saveLabelOfContributor(labelId, contributorId);
            });
        });
    }
}
