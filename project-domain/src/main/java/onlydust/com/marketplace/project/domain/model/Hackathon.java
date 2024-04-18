package onlydust.com.marketplace.project.domain.model;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Value;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;
import onlydust.com.marketplace.kernel.model.UuidWrapper;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

import static onlydust.com.marketplace.kernel.exception.OnlyDustException.badRequest;

@Value
@Accessors(fluent = true)
@EqualsAndHashCode
public class Hackathon {

    private static final Pattern SLUGIFY_REGEX = Pattern.compile("[^a-zA-Z0-9_\\- ]+");

    @NonNull
    Id id;
    @NonNull
    Status status;
    @NonNull
    String title;
    @NonNull
    String subtitle;
    String description;
    String location;
    String totalBudget;
    @NonNull
    ZonedDateTime startDate;
    @NonNull
    ZonedDateTime endDate;
    @NonNull
    List<NamedLink> links = new ArrayList<>();
    @NonNull
    List<UUID> sponsorIds = new ArrayList<>();
    @NonNull
    List<Track> tracks = new ArrayList<>();

    public @NonNull String slug() {
        return title.replaceAll("[^a-zA-Z0-9_\\- ]+", "").trim().replaceAll("\\s+", "-").toLowerCase();
    }

    public Hackathon(@NonNull String title, @NonNull String subtitle, @NonNull ZonedDateTime startDate, @NonNull ZonedDateTime endDate) {
        this.id = Id.random();
        this.status = Status.DRAFT;
        this.title = title;
        this.subtitle = subtitle;
        this.startDate = startDate;
        this.endDate = endDate;
        this.description = null;
        this.location = null;
        this.totalBudget = null;

        if (this.slug().isEmpty()) {
            throw badRequest("Title must contain at least one alphanumeric character");
        }
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

    public enum Status {
        DRAFT,
        PUBLISHED
    }

    public record Track(
            @NonNull String name,
            String subtitle,
            String description,
            String iconSlug,
            @NonNull List<UUID> projectIds
    ) {
    }
}
