package onlydust.com.marketplace.api.postgres.adapter.entity.write.old;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

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
    UUID projectId;
    @Id
    UUID userId;

    @CreationTimestamp
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
