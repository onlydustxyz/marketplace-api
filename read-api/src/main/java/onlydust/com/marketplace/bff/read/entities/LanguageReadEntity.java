package onlydust.com.marketplace.bff.read.entities;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Value;
import lombok.experimental.Accessors;
import onlydust.com.marketplace.api.contract.model.LanguageResponse;
import onlydust.com.marketplace.bff.read.entities.ecosystem.EcosystemReadEntity;
import org.hibernate.annotations.Immutable;

import java.util.Set;
import java.util.UUID;

@Entity
@NoArgsConstructor(force = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Value
@Table(name = "languages", schema = "public")
@Immutable
@Accessors(fluent = true)
public class LanguageReadEntity {
    @Id
    @EqualsAndHashCode.Include
    @NonNull
    UUID id;

    @NonNull
    String name;
    String logoUrl;
    String bannerUrl;


    @ManyToMany
    @JoinTable(name = "ecosystem_languages",
            joinColumns = @JoinColumn(name = "language_id"),
            inverseJoinColumns = @JoinColumn(name = "ecosystem_id"))
    private Set<EcosystemReadEntity> ecosystems;

    public LanguageResponse toDto() {
        return new LanguageResponse()
                .id(id)
                .name(name)
                .logoUrl(logoUrl)
                .bannerUrl(bannerUrl);
    }
}
