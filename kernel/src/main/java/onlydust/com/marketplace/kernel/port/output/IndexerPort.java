package onlydust.com.marketplace.kernel.port.output;

import java.util.List;
import java.util.Set;

public interface IndexerPort {
    void indexUser(Long githubUserId);

    void indexUsers(List<Long> githubUserIds);

    void indexPullRequest(String repoOwner, String repoName, Long pullRequestNumber);

    void indexIssue(String repoOwner, String repoName, Long issueNumber);

    void onRepoLinkChanged(Set<Long> linkedRepoIds, Set<Long> unlinkedRepoIds);
}
