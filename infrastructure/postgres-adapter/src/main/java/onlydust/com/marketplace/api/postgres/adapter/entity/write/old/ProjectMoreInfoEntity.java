package onlydust.com.marketplace.api.postgres.adapter.entity.write.old;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Data
@Builder
@Table(name = "project_more_infos", schema = "public")
@IdClass(ProjectMoreInfoEntity.PrimaryKey.class)
public class ProjectMoreInfoEntity {

    @Id
    @EqualsAndHashCode.Include
    UUID projectId;
    @Id
    @EqualsAndHashCode.Include
    String url;
    String name;
    Integer rank;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    Instant updatedAt;

    @EqualsAndHashCode
    public static class PrimaryKey implements Serializable {
        UUID projectId;
        String url;
    }
}
