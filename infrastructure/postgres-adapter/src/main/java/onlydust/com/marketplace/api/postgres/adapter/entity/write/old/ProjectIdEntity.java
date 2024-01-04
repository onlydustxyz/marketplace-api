package onlydust.com.marketplace.api.postgres.adapter.entity.write.old;

import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Data
@Builder
@Table(name = "projects", schema = "public")
public class ProjectIdEntity {

  @Id
  @Column(name = "id")
  UUID id;
}
