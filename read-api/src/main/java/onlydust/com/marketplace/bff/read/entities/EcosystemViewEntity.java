package onlydust.com.marketplace.bff.read.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.Accessors;
import onlydust.com.marketplace.api.contract.model.EcosystemResponse;
import org.hibernate.annotations.Immutable;

import java.util.UUID;

@Entity
@Table(name = "ecosystems", schema = "public")
@Value
@ToString
@Immutable
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor(force = true)
@Accessors(fluent = true)
public class EcosystemViewEntity {
    @Id
    @EqualsAndHashCode.Include
    @NonNull UUID id;

    @NonNull String name;
    String url;
    @Column(name = "logo_url")
    String logoUrl;
    String bannerUrl;

    public EcosystemResponse toDto() {
        return new EcosystemResponse()
                .id(id)
                .name(name)
                .url(url)
                .logoUrl(logoUrl)
                .bannerUrl(bannerUrl);
    }
}
