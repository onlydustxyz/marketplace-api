package onlydust.com.marketplace.accounting.domain.view;

import lombok.NonNull;

import java.util.UUID;

public record ShortContributorView(
        @NonNull String login,
        String avatarUrl,
        String email,
        UUID id
) {
}
