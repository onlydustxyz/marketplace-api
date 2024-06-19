package onlydust.com.marketplace.api.rest.api.adapter;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.contract.ProjectApplicationsApi;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticatedAppUserService;
import onlydust.com.marketplace.project.domain.model.Application;
import onlydust.com.marketplace.project.domain.port.input.ApplicationFacadePort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

import static org.springframework.http.ResponseEntity.noContent;

@RestController
@Tags(@Tag(name = "Applications"))
@AllArgsConstructor
public class ProjectApplicationsRestApi implements ProjectApplicationsApi {
    private final AuthenticatedAppUserService authenticatedAppUserService;
    private final ApplicationFacadePort applicationFacadePort;

    @Override
    public ResponseEntity<Void> deleteProjectApplication(UUID applicationId) {
        final var authenticatedUser = authenticatedAppUserService.getAuthenticatedUser();
        applicationFacadePort.deleteApplication(Application.Id.of(applicationId), authenticatedUser.getId(), authenticatedUser.getGithubUserId());
        return noContent().build();
    }

}
