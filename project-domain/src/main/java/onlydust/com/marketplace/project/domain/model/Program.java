package onlydust.com.marketplace.project.domain.model;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;
import onlydust.com.marketplace.kernel.model.UuidWrapper;

import java.net.URI;
import java.util.UUID;

@Builder(toBuilder = true)
public record Program(@NonNull Id id,
                      @NonNull String name,
                      @NonNull URI logoUrl) {
    public static Program create(@NonNull String name, URI url, @NonNull URI logoUrl) {
        return Program.builder()
                .id(Id.random())
                .name(name)
                .logoUrl(logoUrl)
                .build();
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
