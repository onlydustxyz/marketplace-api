package onlydust.com.marketplace.api.postgres.adapter.entity.read;

import java.io.Serializable;
import java.util.UUID;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Data
@Entity
@Immutable
public class ProjectLedIdViewEntity {

  @EmbeddedId
  Id id;
  String projectSlug;
  String logoUrl;
  String name;
  Boolean pending;
  Long contributorCount;

  @Embeddable
  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  @Data
  public static class Id implements Serializable {

    UUID userId;
    UUID projectId;
  }
}
