package onlydust.com.marketplace.api.github_api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import onlydust.com.marketplace.project.domain.model.GithubComment;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CommentResponse(Long id) {
    public GithubComment toDomain() {
        return new GithubComment(GithubComment.Id.of(id));
    }
}
