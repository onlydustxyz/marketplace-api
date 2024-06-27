package onlydust.com.marketplace.project.domain.model;

import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;
import onlydust.com.marketplace.kernel.model.UuidWrapper;

import java.net.URI;
import java.time.ZonedDateTime;
import java.util.UUID;

@Getter
@Accessors(fluent = true, chain = true)
@AllArgsConstructor
public class Banner {
    @NonNull
    private final Id id;

    @NonNull
    String text;
    String buttonText;
    String buttonIconSlug;
    URI buttonLinkUrl;
    boolean visible;

    @NonNull
    ZonedDateTime updatedAt;

    public Banner(@NonNull String text, String buttonText, String buttonIconSlug, URI buttonLinkUrl
    ) {
        this(Id.random(), text, buttonText, buttonIconSlug, buttonLinkUrl, false, ZonedDateTime.now());
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
