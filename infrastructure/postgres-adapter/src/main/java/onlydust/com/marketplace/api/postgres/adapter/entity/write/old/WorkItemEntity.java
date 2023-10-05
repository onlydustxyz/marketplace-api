package onlydust.com.marketplace.api.postgres.adapter.entity.write.old;

import com.vladmihalcea.hibernate.type.basic.PostgreSQLEnumType;
import lombok.*;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.type.ContributionTypeEnumEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.type.WorkItemIdEntity;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.*;
import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Data
@Builder
@Table(name = "work_items", schema = "public")
@TypeDef(name = "contribution_type", typeClass = PostgreSQLEnumType.class)
public class WorkItemEntity {


    @EmbeddedId
    WorkItemIdEntity workItemId;
    @Column(name = "id", nullable = false)
    String id;
    @Column(name = "recipient_id", nullable = false)
    Integer recipientId;
    @Column(name = "project_id", nullable = false)
    UUID projectId;
    @Enumerated(EnumType.STRING)
    @Type(type = "contribution_type")
    @Column(name = "type")
    ContributionTypeEnumEntity contributionType;


}
