package onlydust.com.marketplace.api.postgres.adapter.entity.write.old;

import java.io.Serializable;
import java.util.Date;
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
import org.hibernate.annotations.CreationTimestamp;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Data
@Builder
@Table(name = "project_leads", schema = "public")
@IdClass(ProjectLeadEntity.PrimaryKey.class)
public class ProjectLeadEntity {

  @Id
  @Column(name = "project_id", nullable = false, updatable = false)
  UUID projectId;
  @Id
  @Column(name = "user_id", nullable = false, updatable = false)
  UUID userId;

  @CreationTimestamp
  @Column(name = "assigned_at", nullable = false, updatable = false)
  private Date assignedAt;

  public ProjectLeadEntity(UUID projectId, UUID userId) {
    this.projectId = projectId;
    this.userId = userId;
  }

  @NoArgsConstructor
  @AllArgsConstructor
  @EqualsAndHashCode
  public static class PrimaryKey implements Serializable {

    UUID projectId;
    UUID userId;
  }
}
