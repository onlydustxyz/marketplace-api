package onlydust.com.marketplace.api.github_api.adapters;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import onlydust.com.marketplace.api.github_api.GithubHttpClient;
import onlydust.com.marketplace.api.github_api.dto.CommentRequest;
import onlydust.com.marketplace.api.github_api.dto.CommentResponse;
import onlydust.com.marketplace.api.github_api.dto.IssueAssigneesRequest;
import onlydust.com.marketplace.api.github_api.dto.IssueAssigneesResponse;
import onlydust.com.marketplace.project.domain.model.GithubComment;
import onlydust.com.marketplace.project.domain.model.GithubIssue;
import onlydust.com.marketplace.project.domain.port.output.GithubApiPort;

import java.util.List;

import static onlydust.com.marketplace.kernel.exception.OnlyDustException.internalServerError;

@AllArgsConstructor
@Slf4j
public class GithubApiAdapter implements GithubApiPort {
    private final GithubHttpClient client;

    @Override
    public GithubComment.Id createComment(@NonNull String personalAccessToken, @NonNull GithubIssue issue, @NonNull String body) {
        final var request = new CommentRequest(body);
        return client.post("/repositories/%d/issues/%d/comments".formatted(issue.repoId(), issue.number()), request, personalAccessToken, CommentResponse.class)
                .map(CommentResponse::id)
                .map(GithubComment.Id::of)
                .orElseThrow(() -> internalServerError("Failed to create comment"));
    }

    @Override
    public void assign(@NonNull String personalAccessToken, @NonNull Long repoId, @NonNull Long githubIssueNumber, @NonNull String githubLogin) {
        client.post("/repositories/%d/issues/%d/assignees".formatted(repoId, githubIssueNumber),
                        new IssueAssigneesRequest(List.of(githubLogin)), personalAccessToken, IssueAssigneesResponse.class)
                .orElseThrow(() -> internalServerError("Failed to assign user to issue"));
    }

    @Override
    public void unassign(@NonNull String personalAccessToken, @NonNull Long repoId, @NonNull Long githubIssueNumber, @NonNull String githubLogin) {
        client.delete("/repositories/%d/issues/%d/assignees".formatted(repoId, githubIssueNumber),
                        new IssueAssigneesRequest(List.of(githubLogin)), personalAccessToken, IssueAssigneesResponse.class)
                .orElseThrow(() -> internalServerError("Failed to assign user to issue"));
    }

    @Override
    public void deleteComment(@NonNull String personalAccessToken, @NonNull Long repoId, GithubComment.@NonNull Id id) {
        client.delete("/repositories/%d/issues/comments/%d".formatted(repoId, id.value()), personalAccessToken, Void.class);
    }
}
