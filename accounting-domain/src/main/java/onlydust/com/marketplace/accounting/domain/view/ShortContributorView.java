package onlydust.com.marketplace.accounting.domain.view;

import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.model.user.GithubUserId;
import onlydust.com.marketplace.accounting.domain.model.user.UserId;

import java.util.UUID;

public record ShortContributorView(
        @NonNull GithubUserId githubUserId,
        @NonNull String login,
        @NonNull String avatarUrl,
        UserId userId,
        String email
) {
}
