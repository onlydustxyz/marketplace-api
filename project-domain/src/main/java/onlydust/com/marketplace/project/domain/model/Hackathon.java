package onlydust.com.marketplace.project.domain.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;
import onlydust.com.marketplace.kernel.model.UuidWrapper;

import java.time.ZonedDateTime;
import java.util.*;
import java.util.regex.Pattern;

import static onlydust.com.marketplace.kernel.exception.OnlyDustException.badRequest;

@Value
@Accessors(fluent = true)
@EqualsAndHashCode
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Hackathon {

    private static final Pattern SLUGIFY_REGEX = Pattern.compile("[^a-zA-Z0-9_\\- ]+");

    @NonNull
    Id id;
    @NonNull
    Status status;
    @NonNull
    String title;
    String description;
    String location;
    String totalBudget;
    @NonNull
    ZonedDateTime startDate;
    @NonNull
    ZonedDateTime endDate;
    @NonNull
    Set<String> githubLabels = new HashSet<>();
    @NonNull
    Set<NamedLink> communityLinks = new HashSet<>();
    @NonNull
    Set<NamedLink> links = new HashSet<>();
    @NonNull
    Set<UUID> projectIds = new HashSet<>();
    @NonNull
    List<Event> events = new ArrayList<>();

    public @NonNull String slug() {
        return title.replaceAll("[^a-zA-Z0-9_\\- ]+", "").trim().replaceAll("\\s+", "-").toLowerCase();
    }

    public Hackathon(@NonNull String title, @NonNull Collection<String> githubLabels, @NonNull ZonedDateTime startDate, @NonNull ZonedDateTime endDate) {
        this.id = Id.random();
        this.status = Status.DRAFT;
        this.title = title;
        this.githubLabels.addAll(githubLabels);
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

        @JsonCreator
        public static Id of(@NonNull final String uuid) {
            return Id.of(UUID.fromString(uuid));
        }
    }

    public enum Status {
        DRAFT,
        PUBLISHED
    }

    @Builder
    public record Event(@NonNull UUID id,
                        @NonNull String name,
                        @NonNull String subtitle,
                        @NonNull String iconSlug,
                        @NonNull ZonedDateTime startDate,
                        @NonNull ZonedDateTime endDate,
                        @NonNull Set<NamedLink> links) {
    }
}
