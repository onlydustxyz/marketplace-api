package onlydust.com.marketplace.api.postgres.adapter.entity.read;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import onlydust.com.marketplace.project.domain.model.Project;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

@Entity
@NoArgsConstructor
@EqualsAndHashCode
@Table(name = "projects_tags", schema = "public")
@Getter
@EntityListeners(AuditingEntityListener.class)
@Immutable
@IdClass(ProjectTagViewEntity.PrimaryKey.class)
public class ProjectTagViewEntity {

    @Id
    UUID projectId;
    @Id
    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "project_tag")
    Project.Tag tag;

    @Embeddable
    @Getter
    @NoArgsConstructor
    @EqualsAndHashCode
    public static class PrimaryKey implements Serializable {
        UUID projectId;
        Project.Tag tag;
    }


    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    @EqualsAndHashCode.Exclude
    private Date createdAt;
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    @EqualsAndHashCode.Exclude
    private Date updatedAt;
}
