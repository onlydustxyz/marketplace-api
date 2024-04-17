package onlydust.com.marketplace.api.postgres.adapter.entity.write;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcType;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

@Entity
@NoArgsConstructor
@Builder
@AllArgsConstructor
@EqualsAndHashCode
@Table(name = "projects_tags", schema = "public")
@Getter
@EntityListeners(AuditingEntityListener.class)
public class ProjectTagEntity {

    @EmbeddedId
    Id id;

    @Embeddable
    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class Id implements Serializable {
        UUID projectId;
        @Enumerated(EnumType.STRING)
        @JdbcType(PostgreSQLEnumJdbcType.class)
        @Column(columnDefinition = "project_tag")
        ProjectTagEnumEntity tag;
    }


    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    @EqualsAndHashCode.Exclude
    private Date createdAt;
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    @EqualsAndHashCode.Exclude
    private Date updatedAt;

    public enum ProjectTagEnumEntity {
        HOT_COMMUNITY,
        NEWBIES_WELCOME,
        LIKELY_TO_REWARD,
        WORK_IN_PROGRESS,
        FAST_AND_FURIOUS,
        BIG_WHALE,
        UPDATED_ROADMAP
    }
}
