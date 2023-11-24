package onlydust.com.marketplace.api.postgres.adapter.entity.write.old;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Data
@Builder
@Table(name = "project_more_infos", schema = "public")
public class ProjectMoreInfoEntity {

    @EmbeddedId
    Id id;
    String name;

    @EqualsAndHashCode.Exclude
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    Instant createdAt;

    @EqualsAndHashCode.Exclude
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    Instant updatedAt;

    @Embeddable
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    public static class Id implements Serializable {
        @Column(name = "project_id")
        UUID projectId;
        String url;
    }
}
