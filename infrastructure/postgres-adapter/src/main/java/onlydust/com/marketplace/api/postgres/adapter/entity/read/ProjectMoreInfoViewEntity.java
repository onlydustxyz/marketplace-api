package onlydust.com.marketplace.api.postgres.adapter.entity.read;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Data
@Table(name = "project_more_infos", schema = "public")
@IdClass(ProjectMoreInfoViewEntity.PrimaryKey.class)
@Immutable
public class ProjectMoreInfoViewEntity {

    @Id
    UUID projectId;
    @Id
    String url;
    String name;
    Integer rank;

    @EqualsAndHashCode.Exclude
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    Instant createdAt;

    @EqualsAndHashCode.Exclude
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    Instant updatedAt;

    @EqualsAndHashCode
    public static class PrimaryKey implements Serializable {
        UUID projectId;
        String url;
    }
}
