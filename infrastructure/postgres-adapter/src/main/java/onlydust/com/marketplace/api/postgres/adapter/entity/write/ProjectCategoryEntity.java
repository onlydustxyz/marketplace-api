package onlydust.com.marketplace.api.postgres.adapter.entity.write;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
import onlydust.com.marketplace.project.domain.model.ProjectCategory;

import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Data
@Builder(access = AccessLevel.PRIVATE)
@Table(name = "project_categories", schema = "public")
public class ProjectCategoryEntity {
    @Id
    private @NonNull UUID id;
    private @NonNull String name;
    private @NonNull String iconSlug;

    public static ProjectCategoryEntity fromDomain(ProjectCategory projectCategory) {
        return ProjectCategoryEntity.builder()
                .id(projectCategory.id().value())
                .name(projectCategory.name())
                .iconSlug(projectCategory.iconSlug())
                .build();
    }

    public ProjectCategory toDomain() {
        return new ProjectCategory(
                ProjectCategory.Id.of(id),
                name,
                iconSlug);
    }
}
