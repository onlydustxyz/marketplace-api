package onlydust.com.marketplace.api.postgres.adapter.entity.write.old;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Data
@Builder
@Table(name = "project_leads", schema = "public")
public class ProjectLeadEntity {
    @EmbeddedId
    private ProjectLeadEntity.PrimaryKey primaryKey;

    @CreationTimestamp
    @Column(name = "assigned_at", nullable = false, updatable = false)
    private Date assignedAt;

    public ProjectLeadEntity(UUID projectId, UUID userId) {
        this.primaryKey = new PrimaryKey(projectId, userId);
    }

    @Embeddable
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PrimaryKey implements Serializable {
        @Column(name = "project_id", nullable = false)
        UUID projectId;
        @Column(name = "user_id", nullable = false)
        UUID userId;
    }
}
