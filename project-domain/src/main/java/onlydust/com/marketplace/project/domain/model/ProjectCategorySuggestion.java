package onlydust.com.marketplace.project.domain.model;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Value;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;
import onlydust.com.marketplace.kernel.model.UuidWrapper;

import java.util.UUID;

@Value
@Accessors(fluent = true)
public class ProjectCategorySuggestion {
    private final @NonNull Id id;
    private final @NonNull String name;

    public static ProjectCategorySuggestion of(@NonNull final String name) {
        return new ProjectCategorySuggestion(Id.random(), name);
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