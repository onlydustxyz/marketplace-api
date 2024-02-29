package onlydust.com.marketplace.user.domain.model;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;
import onlydust.com.marketplace.kernel.model.UuidWrapper;

import java.util.Set;
import java.util.UUID;

public record BackofficeUser(@NonNull Id id, @NonNull String email, @NonNull String name, @NonNull Set<Role> roles, String avatarUrl) {

    @NoArgsConstructor(staticName = "random")
    @EqualsAndHashCode(callSuper = true)
    @SuperBuilder
    public static class Id extends UuidWrapper {
        public static Id of(@NonNull final UUID uuid) {
            return Id.builder().uuid(uuid).build();
        }

        public static Id of(@NonNull final String uuid) {
            return Id.of(UUID.fromString(uuid));
        }
    }

    public enum Role {
        BO_ADMIN, BO_READER
    }

    @Builder
    public record Identity(@NonNull String email, @NonNull String name, String avatarUrl) {
    }
}
