package onlydust.com.marketplace.api.postgres.adapter.entity.backoffice.read;

import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import onlydust.com.marketplace.project.domain.view.backoffice.EcosystemView;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Data
@Entity
@TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
public class BoEcosystemEntity {
    @Id
    UUID id;
    String name;
    String url;
    String logoUrl;
    @Type(type = "jsonb")
    List<UUID> projectIds;

    public EcosystemView toView() {
        return EcosystemView.builder()
                .id(id)
                .name(name)
                .url(url)
                .logoUrl(logoUrl)
                .projectIds(projectIds)
                .build();
    }
}
