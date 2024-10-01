package onlydust.com.marketplace.api.rest.api.adapter;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import onlydust.com.marketplace.api.contract.ProjectContributorLabelsApi;
import onlydust.com.marketplace.api.contract.model.ProjectContributorLabelRequest;
import onlydust.com.marketplace.api.contract.model.ProjectContributorLabelResponse;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticatedAppUserService;
import onlydust.com.marketplace.kernel.model.ProjectId;
import onlydust.com.marketplace.project.domain.model.ProjectContributorLabel;
import onlydust.com.marketplace.project.domain.port.input.ProjectContributorLabelFacadePort;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@Tags(@Tag(name = "ProjectContributorLabels"))
@AllArgsConstructor
@Slf4j
@Profile("api")
public class ProjectContributorLabelsRestApi implements ProjectContributorLabelsApi {

    private final AuthenticatedAppUserService authenticatedAppUserService;
    private final ProjectContributorLabelFacadePort projectContributorLabelFacadePort;

    @Override
    public ResponseEntity<ProjectContributorLabelResponse> createProjectContributorLabel(UUID projectId,
                                                                                         ProjectContributorLabelRequest projectContributorLabelRequest) {
        final var caller = authenticatedAppUserService.getAuthenticatedUser();
        final var label = projectContributorLabelFacadePort.createLabel(caller.id(), ProjectId.of(projectId), projectContributorLabelRequest.getName());
        return ResponseEntity.ok(new ProjectContributorLabelResponse()
                .id(label.id().value())
                .name(label.name()));
    }

    @Override
    public ResponseEntity<Void> deleteProjectContributorLabel(UUID labelId) {
        final var caller = authenticatedAppUserService.getAuthenticatedUser();
        projectContributorLabelFacadePort.deleteLabel(caller.id(), ProjectContributorLabel.Id.of(labelId));
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<Void> updateProjectContributorLabel(UUID labelId, ProjectContributorLabelRequest projectContributorLabelRequest) {
        final var caller = authenticatedAppUserService.getAuthenticatedUser();
        projectContributorLabelFacadePort.updateLabel(caller.id(), ProjectContributorLabel.Id.of(labelId), projectContributorLabelRequest.getName());
        return ResponseEntity.noContent().build();
    }
}
