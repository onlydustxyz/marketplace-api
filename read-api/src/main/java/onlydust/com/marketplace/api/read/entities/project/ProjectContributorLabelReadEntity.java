package onlydust.com.marketplace.api.read.entities.project;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import onlydust.com.marketplace.api.contract.model.ProjectContributorLabelResponse;
import org.hibernate.annotations.Immutable;

import java.util.UUID;

@NoArgsConstructor(force = true)
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Immutable
@Accessors(fluent = true)
@Entity
@Table(name = "project_contributor_labels", schema = "public")
public class ProjectContributorLabelReadEntity {
    @Id
    private @NonNull UUID id;
    private @NonNull String slug;
    private @NonNull UUID projectId;
    private @NonNull String name;

    public ProjectContributorLabelResponse toDto() {
        return new ProjectContributorLabelResponse()
                .id(id)
                .slug(slug)
                .name(name);
    }
}
