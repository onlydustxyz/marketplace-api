package onlydust.com.marketplace.accounting.domain.view;

import lombok.NonNull;

public record ShortContributorView(
        @NonNull String login,
        String avatarUrl
) {
}
