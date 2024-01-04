package onlydust.com.marketplace.api.postgres.adapter.entity.write.old;

import java.io.Serializable;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

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

  @EqualsAndHashCode
  public static class PrimaryKey implements Serializable {

    UUID projectId;
    Long repoId;
  }
}
