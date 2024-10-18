package onlydust.com.marketplace.api.rest.api.adapter;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.contract.PullRequestsApi;
import onlydust.com.marketplace.api.contract.model.PullRequestPatchRequest;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticatedAppUserService;
import onlydust.com.marketplace.kernel.model.AuthenticatedUser;
import onlydust.com.marketplace.kernel.model.ContributionUUID;
import onlydust.com.marketplace.project.domain.model.GithubIssue;
import onlydust.com.marketplace.project.domain.model.UpdatePullRequestCommand;
import onlydust.com.marketplace.project.domain.port.input.PullRequestFacadePort;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

import static java.util.Objects.isNull;

@RestController
@Tags(@Tag(name = "PullRequests"))
@AllArgsConstructor
@Profile("api")
public class PullRequestRestApi implements PullRequestsApi {
    private final AuthenticatedAppUserService authenticatedAppUserService;
    private final PullRequestFacadePort pullRequestFacadePort;

    @Override
    public ResponseEntity<Void> updatePullRequest(UUID contributionUuid, PullRequestPatchRequest pullRequestPatchRequest) {
        final AuthenticatedUser authenticatedUser = authenticatedAppUserService.getAuthenticatedUser();
        pullRequestFacadePort.updatePullRequest(authenticatedUser.id(),
                new UpdatePullRequestCommand(ContributionUUID.of(contributionUuid), pullRequestPatchRequest.getArchived(),
                        isNull(pullRequestPatchRequest.getLinkedIssues()) ? null :
                                pullRequestPatchRequest.getLinkedIssues().stream().map(GithubIssue.Id::of).toList()));
        return ResponseEntity.noContent().build();
    }
}
