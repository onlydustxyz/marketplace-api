package onlydust.com.marketplace.api.rest.api.adapter;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.contract.ApplicationsApi;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticatedAppUserService;
import onlydust.com.marketplace.project.domain.model.Application;
import onlydust.com.marketplace.project.domain.port.input.UserFacadePort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

import static org.springframework.http.ResponseEntity.noContent;

@RestController
@Tags(@Tag(name = "Applications"))
@AllArgsConstructor
public class ApplicationsRestApi implements ApplicationsApi {
    private final AuthenticatedAppUserService authenticatedAppUserService;
    private final UserFacadePort userFacadePort;

    @Override
    public ResponseEntity<Void> deleteApplication(UUID applicationId) {
        final var authenticatedUser = authenticatedAppUserService.getAuthenticatedUser();
        userFacadePort.deleteApplication(Application.Id.of(applicationId), authenticatedUser.getId(), authenticatedUser.getGithubUserId());
        return noContent().build();
    }

}
