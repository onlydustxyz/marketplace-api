package onlydust.com.marketplace.api.postgres.adapter.entity.write.old;

import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Data
@Builder
@Table(name = "project_github_repos", schema = "public")
@IdClass(ProjectRepoEntity.PrimaryKey.class)
public class ProjectRepoEntity {
    @Id
    @Column(name = "project_id", nullable = false, updatable = false)
    UUID projectId;

    @Id
    @Column(name = "github_repo_id", nullable = false, updatable = false)
    Long repoId;

    public static class PrimaryKey implements Serializable {
        UUID projectId;
        Long repoId;
    }
}
