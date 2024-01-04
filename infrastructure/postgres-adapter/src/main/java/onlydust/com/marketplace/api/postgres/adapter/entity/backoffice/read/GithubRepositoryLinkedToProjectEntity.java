package onlydust.com.marketplace.api.postgres.adapter.entity.backoffice.read;

import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import java.io.Serializable;
import java.util.Map;
import java.util.UUID;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Data
@Entity
@TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
public class GithubRepositoryLinkedToProjectEntity {

  @EmbeddedId
  Id id;
  String name;
  String owner;
  @Type(type = "jsonb")
  Map<String, Long> technologies;

  @Embeddable
  @Data
  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  public static class Id implements Serializable {

    Long id;
    UUID projectId;
  }
}
