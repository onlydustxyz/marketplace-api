package onlydust.com.marketplace.api.postgres.adapter.entity.read;

import javax.persistence.Entity;
import javax.persistence.Id;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Value;

@Entity
@Value
@EqualsAndHashCode
@NoArgsConstructor(force = true)
public class TechnologyViewEntity {

  @Id
  String technology;
}
