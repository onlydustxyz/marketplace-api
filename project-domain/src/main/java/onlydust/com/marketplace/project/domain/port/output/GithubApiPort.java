package onlydust.com.marketplace.project.domain.port.output;

import lombok.NonNull;
import onlydust.com.marketplace.project.domain.model.GithubComment;
import onlydust.com.marketplace.project.domain.model.GithubIssue;

public interface GithubApiPort {
    GithubComment.Id createComment(@NonNull String personalAccessToken, @NonNull GithubIssue issue, @NonNull String body);

    void assign(@NonNull String personalAccessToken, @NonNull Long repoId, @NonNull Long githubIssueNumber, @NonNull String githubLogin);
}
