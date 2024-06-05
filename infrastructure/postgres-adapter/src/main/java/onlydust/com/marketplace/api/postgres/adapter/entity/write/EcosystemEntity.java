package onlydust.com.marketplace.api.postgres.adapter.entity.write;

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
    private @NonNull UUID id;
    private @NonNull String slug;
    private @NonNull String name;
    private @NonNull String logoUrl;
    private String url;
    private @NonNull String description;
    private @NonNull Boolean hidden;

    public static EcosystemEntity fromDomain(final Ecosystem ecosystem) {
        return EcosystemEntity.builder()
                .id(ecosystem.id())
                .slug(ecosystem.slug())
                .name(ecosystem.name())
                .logoUrl(ecosystem.logoUrl())
                .url(ecosystem.url())
                .description(ecosystem.description())
                .hidden(ecosystem.hidden())
                .build();
    }

    public Ecosystem toDomain() {
        return Ecosystem.builder()
                .id(id)
                .logoUrl(logoUrl)
                .url(url)
                .name(name)
                .description(description)
                .hidden(hidden)
                .build();
    }
}
