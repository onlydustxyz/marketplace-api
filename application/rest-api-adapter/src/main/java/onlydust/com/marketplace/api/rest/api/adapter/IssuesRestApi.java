package onlydust.com.marketplace.api.rest.api.adapter;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.contract.IssuesApi;
import onlydust.com.marketplace.api.contract.model.IssuePatchRequest;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticatedAppUserService;
import onlydust.com.marketplace.kernel.model.ContributionUUID;
import onlydust.com.marketplace.project.domain.model.UpdateIssueCommand;
import onlydust.com.marketplace.project.domain.port.input.IssueFacadePort;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@Tags(@Tag(name = "Issues"))
@AllArgsConstructor
@Profile("api")
public class IssuesRestApi implements IssuesApi {
    private final AuthenticatedAppUserService authenticatedAppUserService;
    private final IssueFacadePort issueFacadePort;

    @Override
    public ResponseEntity<Void> updateIssue(UUID contributionUuid, IssuePatchRequest issuePatchRequest) {
        final var authenticatedUser = authenticatedAppUserService.getAuthenticatedUser();
        issueFacadePort.updateIssue(authenticatedUser.id(), new UpdateIssueCommand(ContributionUUID.of(contributionUuid), issuePatchRequest.getArchived()));
        return ResponseEntity.noContent().build();
    }
}
