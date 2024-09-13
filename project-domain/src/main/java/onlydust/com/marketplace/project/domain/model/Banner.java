package onlydust.com.marketplace.project.domain.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;
import onlydust.com.marketplace.kernel.model.UserId;
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
    String shortDescription;
    @NonNull
    String longDescription;
    @NonNull
    String title;
    @NonNull
    String subTitle;
    String buttonText;
    String buttonIconSlug;
    URI buttonLinkUrl;
    boolean visible;

    @NonNull
    ZonedDateTime updatedAt;

    @NonNull
    Set<UserId> closedBy;
    ZonedDateTime date;

    public Banner(final @NonNull String shortDescription, final @NonNull String longDescription, final @NonNull String title, final @NonNull String subTitle,
                  final String buttonText, final String buttonIconSlug, final URI buttonLinkUrl, final ZonedDateTime date
    ) {
        this(Id.random(), shortDescription, longDescription, title, subTitle, buttonText, buttonIconSlug, buttonLinkUrl, false, ZonedDateTime.now(),
                new HashSet<>(), date);
    }

    public void close(final @NonNull UserId userId) {
        closedBy.add(userId);
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
