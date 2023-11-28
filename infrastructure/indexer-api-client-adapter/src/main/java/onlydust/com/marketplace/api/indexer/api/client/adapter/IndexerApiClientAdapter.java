package onlydust.com.marketplace.api.indexer.api.client.adapter;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.domain.port.output.IndexerPort;
import org.springframework.http.HttpMethod;

import java.util.List;
import java.util.Set;

@AllArgsConstructor
public class IndexerApiClientAdapter implements IndexerPort {

    private final IndexerApiHttpClient httpClient;

    @Override
    public void indexUser(Long githubUserId) {
        httpClient.sendRequest("/api/v1/users/" + githubUserId, HttpMethod.PUT, null, Void.class);
    }

    @Override
    public void indexUsers(List<Long> githubUserIds) {
        githubUserIds.stream().parallel().forEach(this::indexUser);
    }

    @Override
    public void indexPullRequest(String repoOwner, String repoName, Long pullRequestNumber) {
        httpClient.sendRequest("/api/v1/repos/%s/%s/pull-requests/%d".formatted(repoOwner, repoName, pullRequestNumber),
                HttpMethod.PUT, null, Void.class);
    }

    @Override
    public void indexIssue(String repoOwner, String repoName, Long issueNumber) {
        httpClient.sendRequest("/api/v1/repos/%s/%s/issues/%d".formatted(repoOwner, repoName, issueNumber),
                HttpMethod.PUT, null, Void.class);
    }

    @Override
    public void onRepoLinkChanged(Set<Long> linkedRepoIds, Set<Long> unlinkedRepoIds) {
        httpClient.sendRequest("/api/v1/events/on-repo-link-changed", HttpMethod.POST, new RepoLinkChangedEvent(linkedRepoIds, unlinkedRepoIds), Void.class);
    }

    private record RepoLinkChangedEvent(Set<Long> linkedRepoIds, Set<Long> unlinkedRepoIds) {
    }
}
