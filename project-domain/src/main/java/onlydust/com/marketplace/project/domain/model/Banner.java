package onlydust.com.marketplace.project.domain.model;

import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;
import onlydust.com.marketplace.kernel.model.UuidWrapper;

import java.net.URI;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Data
@Accessors(fluent = true, chain = true)
@AllArgsConstructor
public class Banner {
    @NonNull
    @Setter(AccessLevel.NONE)
    private final Id id;

    @NonNull
    String text;
    String buttonText;
    String buttonIconSlug;
    URI buttonLinkUrl;
    boolean visible;

    @NonNull
    ZonedDateTime updatedAt;

    @NonNull
    Set<UUID> closedBy;

    public Banner(final @NonNull String text, final String buttonText, final String buttonIconSlug, final URI buttonLinkUrl
    ) {
        this(Id.random(), text, buttonText, buttonIconSlug, buttonLinkUrl, false, ZonedDateTime.now(), new HashSet<>());
    }

    public void close(final @NonNull UUID userId) {
        closedBy.add(userId);
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
