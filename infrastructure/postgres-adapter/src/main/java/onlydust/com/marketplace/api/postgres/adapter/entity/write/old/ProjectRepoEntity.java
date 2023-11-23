package onlydust.com.marketplace.api.postgres.adapter.entity.write.old;

import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Data
@Builder
@Table(name = "project_github_repos", schema = "public")
public class ProjectRepoEntity {
    @EmbeddedId
    @EqualsAndHashCode.Include
    private PrimaryKey primaryKey;

    public ProjectRepoEntity(UUID projectId, Long repoId) {
        this.primaryKey = new PrimaryKey(projectId, repoId);
    }

    @Embeddable
    @AllArgsConstructor
    @NoArgsConstructor
    @EqualsAndHashCode
    @Data
    @Builder
    public static class PrimaryKey implements Serializable {
        @Column(name = "project_id", nullable = false, updatable = false)
        UUID projectId;
        @Column(name = "github_repo_id", nullable = false, updatable = false)
        Long repoId;
    }
}
