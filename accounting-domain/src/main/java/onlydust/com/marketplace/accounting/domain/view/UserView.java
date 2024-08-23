package onlydust.com.marketplace.accounting.domain.view;

import lombok.NonNull;
import onlydust.com.marketplace.kernel.model.UserId;

import java.net.URI;

public record UserView(@NonNull Long githubUserId,
                       @NonNull String githubLogin,
                       @NonNull URI githubAvatarUrl,
                       @NonNull String email,
                       @NonNull UserId id,
                       @NonNull String name) {
}
