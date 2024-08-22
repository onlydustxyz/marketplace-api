package onlydust.com.marketplace.api.read.adapters;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.contract.ReadIssuesApi;
import onlydust.com.marketplace.api.contract.model.GithubIssueResponse;
import onlydust.com.marketplace.api.read.entities.project.ProjectReadEntity;
import onlydust.com.marketplace.api.read.repositories.GithubIssueReadRepository;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticatedAppUserService;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static onlydust.com.marketplace.kernel.exception.OnlyDustException.notFound;
import static org.springframework.http.ResponseEntity.ok;

@RestController
@AllArgsConstructor
@Transactional(readOnly = true)
@Profile("api")
public class ReadIssuesApiPostgresAdapter implements ReadIssuesApi {
    private final AuthenticatedAppUserService authenticatedAppUserService;
    private final GithubIssueReadRepository githubIssueReadRepository;

    @Override
    public ResponseEntity<GithubIssueResponse> getIssue(Long issueId) {
        final var issue = githubIssueReadRepository.findById(issueId)
                .orElseThrow(() -> notFound("Issue %s not found".formatted(issueId)));

        final var issueProjectIds = issue.repo().projects().stream().map(ProjectReadEntity::id).toList();
        final var projectLedIds = authenticatedAppUserService.tryGetAuthenticatedUser()
                .map(user -> user.projectsLed().stream().toList())
                .orElse(List.of());

        final var asProjectLead = projectLedIds.stream().anyMatch(issueProjectIds::contains);
        return ok(issue.toDto(asProjectLead));
    }
}
