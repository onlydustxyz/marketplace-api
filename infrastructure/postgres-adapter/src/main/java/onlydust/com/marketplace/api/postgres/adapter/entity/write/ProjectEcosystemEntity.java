package onlydust.com.marketplace.api.postgres.adapter.entity.write;

import lombok.*;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.ProjectRepoEntity;

import javax.persistence.*;
import java.io.Serializable;
import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Data
@Builder
@Table(name = "projects_ecosystems", schema = "public")
@IdClass(ProjectEcosystemEntity.PrimaryKey.class)
public class ProjectEcosystemEntity {

    @Id
    @Column(name = "project_id", nullable = false, updatable = false)
    UUID projectId;

    @Id
    @Column(name = "ecosystem_id", nullable = false, updatable = false)
    UUID ecosystemId;

    @EqualsAndHashCode
    public static class PrimaryKey implements Serializable {
        UUID projectId;
        UUID ecosystemId;
    }

}
