package onlydust.com.marketplace.project.domain.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;
import onlydust.com.marketplace.kernel.model.UuidWrapper;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Data
@Accessors(fluent = true, chain = true)
public class ProjectCategory {
    private final @NonNull Id id;
    private @NonNull String name;
    private @NonNull String description;
    private @NonNull String iconSlug;
    private @NonNull Set<UUID> projects;

    public static ProjectCategory of(@NonNull final String name, @NonNull final String description, @NonNull final String iconSlug) {
        return new ProjectCategory(Id.random(), name, description, iconSlug, new HashSet<>());
    }

    public String slug() {
        return name.replaceAll("[^a-zA-Z0-9_\\- ]+", "")
                .replaceAll("\s+", "-")
                .toLowerCase();
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
