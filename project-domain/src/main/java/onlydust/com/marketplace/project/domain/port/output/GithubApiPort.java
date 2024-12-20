package onlydust.com.marketplace.project.domain.port.output;

import lombok.NonNull;
import onlydust.com.marketplace.project.domain.model.GithubComment;

public interface GithubApiPort {
    GithubComment.Id createComment(@NonNull String personalAccessToken, @NonNull Long repoId, @NonNull Long githubIssueNumber, @NonNull String body);

    void assign(@NonNull String personalAccessToken, @NonNull Long repoId, @NonNull Long githubIssueNumber, @NonNull String githubLogin);

    void unassign(@NonNull String personalAccessToken, @NonNull Long repoId, @NonNull Long githubIssueNumber, @NonNull String githubLogin);

    void updateComment(@NonNull String githubPersonalToken, @NonNull Long repoId, @NonNull GithubComment.Id id, @NonNull String body);

    void deleteComment(@NonNull String personalAccessToken, @NonNull Long repoId, @NonNull GithubComment.Id id);

    void closeIssue(@NonNull String githubAppAccessToken, @NonNull Long repoId, @NonNull Long githubIssueNumber);
}
