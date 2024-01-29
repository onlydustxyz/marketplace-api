package onlydust.com.marketplace.api.postgres.adapter.entity.write;

import com.vladmihalcea.hibernate.type.basic.PostgreSQLEnumType;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
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
@TypeDef(name = "project_tag", typeClass = PostgreSQLEnumType.class)
@EntityListeners(AuditingEntityListener.class)
public class ProjectTagEntity {

    @EmbeddedId
    Id id;

    @Embeddable
    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Id implements Serializable {
        UUID projectId;
        @Type(type = "project_tag")
        @Enumerated(EnumType.STRING)
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
        BEGINNERS_WELCOME, STRONG_EXPERTISE, LIKELY_TO_SEND_REWARDS, FAST_PACED
    }
}
