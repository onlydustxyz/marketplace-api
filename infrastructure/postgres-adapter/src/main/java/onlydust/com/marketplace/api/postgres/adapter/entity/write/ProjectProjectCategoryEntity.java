package onlydust.com.marketplace.api.postgres.adapter.entity.write;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Data
@Builder(access = AccessLevel.PRIVATE)
@Table(name = "projects_project_categories", schema = "public")
@IdClass(ProjectProjectCategoryEntity.PrimaryKey.class)
public class ProjectProjectCategoryEntity {
    @Id
    @EqualsAndHashCode.Include
    @Column(name = "project_id", nullable = false, updatable = false)
    UUID projectId;

    @Id
    @EqualsAndHashCode.Include
    @Column(name = "project_category_id", nullable = false, updatable = false)
    UUID projectCategoryId;

    @EqualsAndHashCode
    public static class PrimaryKey implements Serializable {
        UUID projectId;
        UUID projectCategoryId;
    }
}
