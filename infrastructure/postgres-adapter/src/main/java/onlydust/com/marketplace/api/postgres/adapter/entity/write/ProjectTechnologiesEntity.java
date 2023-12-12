package onlydust.com.marketplace.api.postgres.adapter.entity.write;


import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Data
@Builder(toBuilder = true)
@Table(name = "project_technologies", schema = "public")
@EntityListeners(AuditingEntityListener.class)
@TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
public class ProjectTechnologiesEntity {

    @Id
    UUID projectId;
    
    @Type(type = "jsonb")
    @Column(name = "languages", columnDefinition = "jsonb")
    Map<String, Long> languages;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Date createdAt;
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Date updatedAt;

    public ProjectTechnologiesEntity(UUID projectId, Map<String, Long> languages) {
        this.projectId = projectId;
        this.languages = languages;
    }
}
