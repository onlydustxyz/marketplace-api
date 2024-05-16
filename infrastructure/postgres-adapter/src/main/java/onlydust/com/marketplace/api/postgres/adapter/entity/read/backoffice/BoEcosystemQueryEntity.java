package onlydust.com.marketplace.api.postgres.adapter.entity.read.backoffice;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import onlydust.com.marketplace.project.domain.view.backoffice.EcosystemView;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.List;
import java.util.UUID;

@NoArgsConstructor
@EqualsAndHashCode
@Data
@Entity
@Immutable
public class BoEcosystemQueryEntity {
    @Id
    UUID id;
    String name;
    String url;
    String logoUrl;
    @JdbcTypeCode(SqlTypes.JSON)
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
