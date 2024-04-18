package onlydust.com.marketplace.api.postgres.adapter.entity.write.old;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.*;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

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
