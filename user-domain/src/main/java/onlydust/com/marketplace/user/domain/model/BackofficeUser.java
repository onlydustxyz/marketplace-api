package onlydust.com.marketplace.user.domain.model;

import lombok.Builder;
import lombok.NonNull;

import java.util.Set;

public record BackofficeUser(@NonNull String email, @NonNull String name, @NonNull Set<Role> roles, String avatarUrl) {

    public enum Role {
        BO_ADMIN, BO_READER
    }

    @Builder
    public record Identity(@NonNull String email, @NonNull String name, String avatarUrl) {
    }
}
