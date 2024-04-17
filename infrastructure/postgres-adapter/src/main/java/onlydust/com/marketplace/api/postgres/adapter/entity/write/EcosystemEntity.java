package onlydust.com.marketplace.api.postgres.adapter.entity.write;

import lombok.*;
import onlydust.com.marketplace.project.domain.model.Ecosystem;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Data
@Builder
@Table(name = "ecosystems", schema = "public")
public class EcosystemEntity {

    @Id
    @Column(name = "id")
    UUID id;
    @Column(name = "name", nullable = false)
    String name;
    @Column(name = "logo_url", nullable = false)
    String logoUrl;
    @Column(name = "url")
    String url;

    public static EcosystemEntity fromDomain(final Ecosystem ecosystem) {
        return EcosystemEntity.builder()
                .id(ecosystem.getId())
                .url(ecosystem.getUrl())
                .name(ecosystem.getName())
                .logoUrl(ecosystem.getLogoUrl())
                .build();
    }

    public Ecosystem toDomain() {
        return Ecosystem.builder()
                .id(this.id)
                .logoUrl(this.logoUrl)
                .url(this.url)
                .name(this.name)
                .build();
    }
}
