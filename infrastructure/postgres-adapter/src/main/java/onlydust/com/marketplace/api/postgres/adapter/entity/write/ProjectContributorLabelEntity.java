package onlydust.com.marketplace.api.postgres.adapter.entity.write;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
import onlydust.com.marketplace.kernel.model.ProjectId;
import onlydust.com.marketplace.project.domain.model.ProjectContributorLabel;

import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Data
@Builder(access = AccessLevel.PRIVATE)
@Table(name = "project_contributor_labels", schema = "public")
public class ProjectContributorLabelEntity {
    @Id
    private @NonNull UUID id;
    private @NonNull String slug;
    private @NonNull UUID projectId;
    private @NonNull String name;

    public static ProjectContributorLabelEntity fromDomain(ProjectContributorLabel label) {
        return ProjectContributorLabelEntity.builder()
                .id(label.id().value())
                .slug(label.slug())
                .projectId(label.projectId().value())
                .name(label.name())
                .build();
    }

    public ProjectContributorLabel toDomain() {
        return new ProjectContributorLabel(
                ProjectContributorLabel.Id.of(id),
                ProjectId.of(projectId),
                name
        );
    }
}
