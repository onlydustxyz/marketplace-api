package onlydust.com.marketplace.api.postgres.adapter.entity.write.old;

import java.io.Serializable;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Data
@Builder
@Table(name = "ignored_contributions", schema = "public")
public class IgnoredContributionEntity {

  @EmbeddedId
  Id id;

  @Builder
  @Data
  @Embeddable
  @NoArgsConstructor
  @AllArgsConstructor
  public static class Id implements Serializable {

    @Column(name = "project_id")
    UUID projectId;
    @Column(name = "contribution_id")
    String contributionId;
  }
}
