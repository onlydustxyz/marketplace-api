package onlydust.com.marketplace.api.read.adapters;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.contract.ReadIssuesApi;
import onlydust.com.marketplace.api.contract.model.GithubIssueResponse;
import onlydust.com.marketplace.api.read.entities.github.GithubIssueReadEntity;
import onlydust.com.marketplace.api.read.repositories.GithubIssueReadRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

import static onlydust.com.marketplace.kernel.exception.OnlyDustException.notFound;
import static org.springframework.http.ResponseEntity.ok;

@RestController
@AllArgsConstructor
@Transactional(readOnly = true)
@Profile("api")
public class ReadIssuesApiPostgresAdapter implements ReadIssuesApi {
    private final GithubIssueReadRepository githubIssueReadRepository;

    @Override
    public ResponseEntity<GithubIssueResponse> getIssue(Long issueId) {
        final var issue = githubIssueReadRepository.findById(issueId);
        return ok(issue.map(GithubIssueReadEntity::toDto).orElseThrow(() -> notFound("Issue %s not found".formatted(issueId))));
    }
}
