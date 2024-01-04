package onlydust.com.marketplace.api.postgres.adapter.entity.write.old;

import com.vladmihalcea.hibernate.type.basic.PostgreSQLEnumType;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.type.ContributionTypeEnumEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.type.WorkItemIdEntity;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

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
  Long recipientId;
  @Column(name = "project_id", nullable = false)
  UUID projectId;
  @Enumerated(EnumType.STRING)
  @Type(type = "contribution_type")
  @Column(name = "type")
  ContributionTypeEnumEntity contributionType;


}
