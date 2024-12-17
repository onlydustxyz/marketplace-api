package onlydust.com.marketplace.api.read.entities;

import java.util.Set;
import java.util.UUID;

import org.hibernate.annotations.Immutable;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import onlydust.com.marketplace.api.contract.model.LanguageResponse;
import onlydust.com.marketplace.api.read.entities.ecosystem.EcosystemReadEntity;

@Entity
@NoArgsConstructor(force = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Table(name = "languages", schema = "public")
@Immutable
@Accessors(fluent = true)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class LanguageReadEntity {
    @Id
    @EqualsAndHashCode.Include
    @NonNull
    UUID id;
    @JsonProperty("name")
    @NonNull
    String name;
    @NonNull
    String slug;
    String logoUrl;
    String bannerUrl;
    String transparentLogoUrl;
    String color;

    @ManyToMany
    @JoinTable(name = "ecosystem_languages",
            joinColumns = @JoinColumn(name = "language_id"),
            inverseJoinColumns = @JoinColumn(name = "ecosystem_id"))
    private Set<EcosystemReadEntity> ecosystems;

    public LanguageResponse toDto() {
        return new LanguageResponse()
                .id(id)
                .slug(slug)
                .name(name)
                .logoUrl(logoUrl)
                .transparentLogoUrl(transparentLogoUrl)
                .bannerUrl(bannerUrl)
                .color(color);
    }
}
