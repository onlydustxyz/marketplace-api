package onlydust.com.marketplace.api.read.entities.github;

import lombok.NonNull;

public record GithubUserLink(@NonNull Long id, @NonNull String login, @NonNull String avatarUrl) {
}
