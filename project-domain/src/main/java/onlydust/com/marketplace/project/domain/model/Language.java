package onlydust.com.marketplace.project.domain.model;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;
import onlydust.com.marketplace.kernel.model.UuidWrapper;

import java.net.URI;
import java.util.Set;
import java.util.UUID;

public record Language(@NonNull Id id,
                       @NonNull String name,
                       @NonNull Set<String> fileExtensions,
                       URI logoUrl,
                       URI bannerUrl) {

    public static Language of(@NonNull final String name, @NonNull final Set<String> fileExtensions) {
        return new Language(Id.random(), name, fileExtensions, null, null);
    }

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
}
