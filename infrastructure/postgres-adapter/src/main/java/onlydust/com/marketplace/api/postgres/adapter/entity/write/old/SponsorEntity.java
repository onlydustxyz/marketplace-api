package onlydust.com.marketplace.api.postgres.adapter.entity.write.old;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
import onlydust.com.marketplace.accounting.domain.view.SponsorView;
import onlydust.com.marketplace.kernel.model.SponsorId;
import onlydust.com.marketplace.project.domain.model.Sponsor;

import java.net.URI;
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

    public Sponsor toDomain() {
        return Sponsor.builder()
                .id(SponsorId.of(id))
                .name(name)
                .logoUrl(URI.create(logoUrl))
                .url(url == null ? null : URI.create(url))
                .build();
    }

    public static SponsorEntity of(Sponsor sponsor) {
        return SponsorEntity.builder()
                .id(sponsor.id().value())
                .name(sponsor.name())
                .logoUrl(sponsor.logoUrl().toString())
                .url(sponsor.url() == null ? null : sponsor.url().toString())
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
}
