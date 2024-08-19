package onlydust.com.marketplace.project.domain.view;

import lombok.NonNull;

public record GithubUserWithTelegramView(@NonNull String githubLogin, @NonNull String telegram) {
}
