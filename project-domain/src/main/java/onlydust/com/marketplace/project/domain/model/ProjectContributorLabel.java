package onlydust.com.marketplace.project.domain.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;
import onlydust.com.marketplace.kernel.model.ProjectId;
import onlydust.com.marketplace.kernel.model.UuidWrapper;

import java.util.UUID;

import static onlydust.com.marketplace.kernel.exception.OnlyDustException.badRequest;

@Data
@Accessors(fluent = true, chain = true)
public class ProjectContributorLabel {
    private final @NonNull Id id;
    private final @NonNull ProjectId projectId;
    private @NonNull String name;

    public ProjectContributorLabel(@NonNull Id id, @NonNull ProjectId projectId, @NonNull String name) {
        this.id = id;
        this.projectId = projectId;
        this.name(name);
    }

    public static ProjectContributorLabel of(@NonNull ProjectId projectId, @NonNull String name) {
        return new ProjectContributorLabel(Id.random(), projectId, name);
    }

    public @NonNull String slug() {
        return name.replaceAll("[^a-zA-Z0-9_\\- ]+", "").trim().replaceAll("\\s+", "-").toLowerCase();
    }

    public void name(@NonNull String name) {
        this.name = name;
        if (this.slug().isEmpty()) {
            throw badRequest("Label must contain at least one alphanumeric character");
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
}
