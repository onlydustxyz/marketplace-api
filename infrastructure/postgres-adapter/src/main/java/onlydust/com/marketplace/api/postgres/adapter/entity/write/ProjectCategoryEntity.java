package onlydust.com.marketplace.api.postgres.adapter.entity.write;

import jakarta.persistence.*;
import lombok.*;
import onlydust.com.marketplace.project.domain.model.ProjectCategory;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Data
@Builder
@Table(name = "project_categories", schema = "public")
public class ProjectCategoryEntity {

    @Id
    UUID id;
    String name;
    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Column(columnDefinition = "project_category_status")
    ProjectCategoryStatusEntity status;
    String iconUrl;

    public static ProjectCategoryEntity fromDomain(ProjectCategory projectCategory) {
        return ProjectCategoryEntity.builder()
                .id(projectCategory.id())
                .name(projectCategory.name())
                .iconUrl(projectCategory.iconUrl())
                .status(ProjectCategoryStatusEntity.fromDomain(projectCategory))
                .build();
    }

    public enum ProjectCategoryStatusEntity {
        SUGGESTED, APPROVED;

        public static ProjectCategoryStatusEntity fromDomain(ProjectCategory projectCategory) {
            return switch (projectCategory.status()) {
                case APPROVED -> APPROVED;
                case SUGGESTED -> SUGGESTED;
            };
        }

        public ProjectCategory.Status toDomain() {
            return switch (this) {
                case APPROVED -> ProjectCategory.Status.APPROVED;
                case SUGGESTED -> ProjectCategory.Status.SUGGESTED;
            };
        }
    }
}
