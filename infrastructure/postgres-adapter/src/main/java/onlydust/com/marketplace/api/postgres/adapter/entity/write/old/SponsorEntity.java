package onlydust.com.marketplace.api.postgres.adapter.entity.write.old;

import jakarta.persistence.*;
import lombok.*;
import onlydust.com.marketplace.accounting.domain.view.SponsorView;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.SponsorLeadEntity;
import onlydust.com.marketplace.kernel.model.SponsorId;
import onlydust.com.marketplace.kernel.model.UserId;
import onlydust.com.marketplace.project.domain.model.Sponsor;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder(toBuilder = true)
@Table(name = "sponsors", schema = "public")
public class SponsorEntity {
    @Id
    @NonNull
    UUID id;
    @NonNull
    String name;
    @NonNull
    String logoUrl;
    String url;
    @OneToMany(mappedBy = "sponsorId", cascade = CascadeType.ALL, orphanRemoval = true)
    List<SponsorLeadEntity> leads;

    public Sponsor toDomain() {
        return Sponsor.builder()
                .id(SponsorId.of(id))
                .name(name)
                .logoUrl(URI.create(logoUrl))
                .url(url == null ? null : URI.create(url))
                .leads(leads.stream()
                        .map(SponsorLeadEntity::getUserId)
                        .map(UserId::of)
                        .toList())
                .build();
    }

    public static SponsorEntity of(Sponsor sponsor) {
        return SponsorEntity.builder()
                .id(sponsor.id().value())
                .name(sponsor.name())
                .logoUrl(sponsor.logoUrl().toString())
                .url(sponsor.url() == null ? null : sponsor.url().toString())
                .leads(sponsor.leads().stream()
                        .map(l -> new SponsorLeadEntity(l.value(), sponsor.id().value()))
                        .toList())
                .build();
    }

    public SponsorView toView() {
        return SponsorView.builder()
                .id(SponsorId.of(id))
                .name(name)
                .logoUrl(URI.create(logoUrl))
                .url(url == null ? null : URI.create(url))
                .build();
    }

    public void updateWith(Sponsor sponsor) {
        name = sponsor.name();
        logoUrl = sponsor.logoUrl().toString();
        url = sponsor.url() == null ? null : sponsor.url().toString();
        leads.removeIf(l -> !sponsor.leads().contains(UserId.of(l.getUserId())));
        leads.addAll(sponsor.leads().stream()
                .filter(l -> leads.stream().noneMatch(sl -> sl.getUserId().equals(l.value())))
                .map(l -> new SponsorLeadEntity(l.value(), id))
                .toList());
    }
}
