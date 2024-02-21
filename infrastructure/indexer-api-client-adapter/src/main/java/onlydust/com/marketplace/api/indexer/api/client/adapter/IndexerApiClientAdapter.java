package onlydust.com.marketplace.api.indexer.api.client.adapter;

import io.netty.handler.codec.http.HttpMethod;
import lombok.AllArgsConstructor;
import onlydust.com.marketplace.project.domain.port.output.IndexerPort;

import java.util.List;
import java.util.Set;

@AllArgsConstructor
public class IndexerApiClientAdapter implements IndexerPort {

    private final IndexerApiHttpClient httpClient;

    @Override
    public void indexUser(Long githubUserId) {
        httpClient.send("/api/v1/users/" + githubUserId, HttpMethod.PUT, null, Void.class);
    }

    @Override
    public void indexUsers(List<Long> githubUserIds) {
        githubUserIds.stream().parallel().forEach(this::indexUser);
    }

    @Override
    public void indexPullRequest(String repoOwner, String repoName, Long pullRequestNumber) {
        httpClient.send("/api/v1/repos/%s/%s/pull-requests/%d".formatted(repoOwner, repoName, pullRequestNumber),
                HttpMethod.PUT, null, Void.class);
    }

    @Override
    public void indexIssue(String repoOwner, String repoName, Long issueNumber) {
        httpClient.send("/api/v1/repos/%s/%s/issues/%d".formatted(repoOwner, repoName, issueNumber),
                HttpMethod.PUT, null, Void.class);
    }

    @Override
    public void onRepoLinkChanged(Set<Long> linkedRepoIds, Set<Long> unlinkedRepoIds) {
        httpClient.send("/api/v1/events/on-repo-link-changed", HttpMethod.POST, new RepoLinkChangedEvent(linkedRepoIds, unlinkedRepoIds), Void.class);
    }

    private record RepoLinkChangedEvent(Set<Long> linkedRepoIds, Set<Long> unlinkedRepoIds) {
    }
}
