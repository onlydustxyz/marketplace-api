package onlydust.com.marketplace.api.postgres.adapter.entity.write;

import jakarta.persistence.*;
import lombok.*;
import onlydust.com.marketplace.project.domain.model.ProjectCategory;

import java.util.Set;
import java.util.UUID;

import static java.util.stream.Collectors.toSet;

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
    private @NonNull String slug;
    private @NonNull String name;
    private @NonNull String description;
    private @NonNull String iconSlug;

    @OneToMany(mappedBy = "projectCategoryId", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ProjectProjectCategoryEntity> projectCategories;

    public static ProjectCategoryEntity fromDomain(ProjectCategory projectCategory) {
        return ProjectCategoryEntity.builder()
                .id(projectCategory.id().value())
                .slug(projectCategory.slug())
                .name(projectCategory.name())
                .description(projectCategory.description())
                .iconSlug(projectCategory.iconSlug())
                .projectCategories(projectCategory.projects().stream()
                        .map(projectId -> new ProjectProjectCategoryEntity(projectId, projectCategory.id().value()))
                        .collect(toSet()))
                .build();
    }

    public ProjectCategory toDomain() {
        return new ProjectCategory(
                ProjectCategory.Id.of(id),
                name,
                description,
                iconSlug,
                projectCategories.stream().map(ProjectProjectCategoryEntity::getProjectId).collect(toSet()));
    }
}
