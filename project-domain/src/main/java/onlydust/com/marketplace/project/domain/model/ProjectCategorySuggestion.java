package onlydust.com.marketplace.project.domain.model;

import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;
import onlydust.com.marketplace.kernel.model.ProjectId;
import onlydust.com.marketplace.kernel.model.UuidWrapper;

import java.util.UUID;

@Value
@Accessors(fluent = true)
@AllArgsConstructor
public class ProjectCategorySuggestion {
    @NonNull
    Id id;
    @NonNull
    String name;
    @NonNull
    ProjectId projectId;

    public static ProjectCategorySuggestion of(@NonNull final String name, @NonNull final ProjectId projectId) {
        return new ProjectCategorySuggestion(Id.random(), name, projectId);
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
