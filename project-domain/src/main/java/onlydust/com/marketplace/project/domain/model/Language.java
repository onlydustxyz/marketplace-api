package onlydust.com.marketplace.project.domain.model;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Value;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;
import onlydust.com.marketplace.kernel.model.UuidWrapper;

import java.net.URI;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Value
@Accessors(fluent = true)
public class Language {
    @NonNull Id id;
    @NonNull String name;
    @NonNull Set<String> fileExtensions;
    URI logoUrl;
    URI bannerUrl;

    public Language(@NonNull Id id, @NonNull String name, @NonNull Set<String> fileExtensions, URI logoUrl, URI bannerUrl) {
        this.id = id;
        this.name = name;
        this.fileExtensions = fileExtensions.stream().map(e -> e.toLowerCase().trim()).collect(Collectors.toSet());
        this.logoUrl = logoUrl;
        this.bannerUrl = bannerUrl;
    }

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
