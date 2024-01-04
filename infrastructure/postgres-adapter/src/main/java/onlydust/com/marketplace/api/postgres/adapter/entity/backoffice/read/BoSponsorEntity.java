package onlydust.com.marketplace.api.postgres.adapter.entity.backoffice.read;

import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import java.util.List;
import java.util.UUID;
import javax.persistence.Entity;
import javax.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import onlydust.com.marketplace.api.domain.view.backoffice.SponsorView;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Data
@Entity
@TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
public class BoSponsorEntity {

  @Id
  UUID id;
  String name;
  String url;
  String logoUrl;
  @Type(type = "jsonb")
  List<UUID> projectIds;

  public SponsorView toView() {
    return SponsorView.builder()
        .id(id)
        .name(name)
        .url(url)
        .logoUrl(logoUrl)
        .projectIds(projectIds)
        .build();
  }
}
