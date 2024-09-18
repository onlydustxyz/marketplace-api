package onlydust.com.marketplace.api.postgres.adapter.entity.write;

import jakarta.persistence.*;
import lombok.*;
import onlydust.com.marketplace.kernel.model.UserId;
import onlydust.com.marketplace.project.domain.model.Ecosystem;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "ecosystems", schema = "public")
@Getter
public class EcosystemEntity {

    @Id
    private @NonNull UUID id;
    private @NonNull String slug;
    private @NonNull String name;
    private @NonNull String logoUrl;
    private String url;
    private String description;
    private @NonNull Boolean hidden;
    @OneToMany(mappedBy = "ecosystemId", cascade = CascadeType.ALL, orphanRemoval = true)
    List<EcosystemLeadEntity> leads;

    public static EcosystemEntity of(final Ecosystem ecosystem) {
        return EcosystemEntity.builder()
                .id(ecosystem.id())
                .slug(ecosystem.slug())
                .name(ecosystem.name())
                .logoUrl(ecosystem.logoUrl().toString())
                .url(ecosystem.url().toString())
                .description(ecosystem.description())
                .hidden(ecosystem.hidden())
                .leads(ecosystem.leads().stream()
                        .map(l -> new EcosystemLeadEntity(l.value(), ecosystem.id()))
                        .toList())
                .build();
    }

    public Ecosystem toDomain() {
        return Ecosystem.builder()
                .id(id)
                .logoUrl(URI.create(logoUrl))
                .url(url == null ? null : URI.create(url))
                .name(name)
                .description(description)
                .hidden(hidden)
                .leads(leads.stream()
                        .map(EcosystemLeadEntity::getUserId)
                        .map(UserId::of)
                        .toList())
                .build();
    }

    public void updateWith(@NonNull Ecosystem ecosystem) {
        name = ecosystem.name();
        logoUrl = ecosystem.logoUrl().toString();
        url = ecosystem.url().toString();
        description = ecosystem.description();
        hidden = ecosystem.hidden();
        leads.removeIf(l -> !ecosystem.leads().contains(UserId.of(l.getUserId())));
        leads.addAll(ecosystem.leads().stream()
                .filter(l -> leads.stream().noneMatch(sl -> sl.getUserId().equals(l.value())))
                .map(l -> new EcosystemLeadEntity(l.value(), id))
                .toList());
    }
}
