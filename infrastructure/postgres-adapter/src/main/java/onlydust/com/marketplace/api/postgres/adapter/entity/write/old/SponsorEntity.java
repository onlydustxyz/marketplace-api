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
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Data
@Builder
@Table(name = "sponsors", schema = "public")
public class SponsorEntity {

  @Id
  @Column(name = "id")
  UUID id;
  @Column(name = "name", nullable = false)
  String name;
  @Column(name = "logo_url", nullable = false)
  String logoUrl;
  @Column(name = "url")
  String url;
}
