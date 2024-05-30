package onlydust.com.marketplace.api.postgres.adapter.entity.write;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
import onlydust.com.marketplace.project.domain.model.Ecosystem;

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
    @Column(name = "slug", nullable = false)
    String slug;
    @Column(name = "name", nullable = false)
    String name;
    @Column(name = "logo_url", nullable = false)
    String logoUrl;
    @Column(name = "url")
    String url;
    @Column(name = "description", nullable = false)
    String description;

    public static EcosystemEntity fromDomain(final Ecosystem ecosystem) {
        return EcosystemEntity.builder()
                .id(ecosystem.getId())
                .slug(ecosystem.getSlug())
                .name(ecosystem.getName())
                .logoUrl(ecosystem.getLogoUrl())
                .url(ecosystem.getUrl())
                .description(ecosystem.getDescription())
                .build();
    }

    public Ecosystem toDomain() {
        return Ecosystem.builder()
                .id(this.id)
                .logoUrl(this.logoUrl)
                .url(this.url)
                .name(this.name)
                .description(this.description)
                .build();
    }
}
