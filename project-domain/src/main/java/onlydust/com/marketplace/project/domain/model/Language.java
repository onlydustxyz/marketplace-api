package onlydust.com.marketplace.project.domain.model;

import java.net.URI;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonCreator;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Value;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;
import onlydust.com.marketplace.kernel.model.UuidWrapper;

@Value
@Accessors(fluent = true)
public class Language {
    @NonNull
    Id id;
    @NonNull
    String name;
    @NonNull
    String slug;
    @NonNull
    Set<String> fileExtensions;
    URI logoUrl;
    URI bannerUrl;
    URI transparentLogoUrl;
    String color;

    public Language(@NonNull Id id, @NonNull String name, @NonNull String slug, @NonNull Set<String> fileExtensions, URI logoUrl, URI transparentLogoUrl, URI bannerUrl, String color) {
        this.id = id;
        this.name = name;
        this.slug = slug;
        this.fileExtensions = fileExtensions.stream().map(e -> e.toLowerCase().trim()).collect(Collectors.toSet());
        this.logoUrl = logoUrl;
        this.transparentLogoUrl = transparentLogoUrl;
        this.bannerUrl = bannerUrl;
        this.color = color;
    }

    @NoArgsConstructor(staticName = "random")
    @EqualsAndHashCode(callSuper = true)
    @SuperBuilder
    public static class Id extends UuidWrapper {
        public static Id of(@NonNull final UUID uuid) {
            return Id.builder().uuid(uuid).build();
        }

        @JsonCreator
        public static Id of(@NonNull final String uuid) {
            return Id.of(UUID.fromString(uuid));
        }
    }
}
