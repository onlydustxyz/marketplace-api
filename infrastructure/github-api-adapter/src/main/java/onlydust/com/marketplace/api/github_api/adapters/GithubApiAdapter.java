package onlydust.com.marketplace.api.github_api.adapters;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import onlydust.com.marketplace.api.github_api.GithubHttpClient;
import onlydust.com.marketplace.api.github_api.dto.CommentRequest;
import onlydust.com.marketplace.api.github_api.dto.CommentResponse;
import onlydust.com.marketplace.project.domain.model.GithubComment;
import onlydust.com.marketplace.project.domain.model.GithubIssue;
import onlydust.com.marketplace.project.domain.port.output.GithubApiPort;

import static onlydust.com.marketplace.kernel.exception.OnlyDustException.internalServerError;

@AllArgsConstructor
@Slf4j
public class GithubApiAdapter implements GithubApiPort {
    private final GithubHttpClient client;

    @Override
    public GithubComment createComment(@NonNull String personalAccessToken, @NonNull GithubIssue issue, @NonNull String body) {
        final var request = new CommentRequest(body);
        return client.post("/repository/%d/issues/%d/comments".formatted(issue.repoId(), issue.number()), request, personalAccessToken, CommentResponse.class)
                .map(CommentResponse::toDomain)
                .orElseThrow(() -> internalServerError("Failed to create comment"));
    }
}
