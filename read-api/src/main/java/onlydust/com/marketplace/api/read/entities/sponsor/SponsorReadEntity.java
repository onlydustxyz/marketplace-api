package onlydust.com.marketplace.api.read.entities.sponsor;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import onlydust.com.marketplace.api.contract.model.SponsorLinkResponse;
import onlydust.com.marketplace.api.contract.model.SponsorResponse;
import org.hibernate.annotations.Immutable;

import java.util.Set;
import java.util.UUID;

@Entity
@NoArgsConstructor(force = true)
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Table(name = "sponsors", schema = "public")
@Immutable
@Accessors(fluent = true)
public class SponsorReadEntity {
    @Id
    @NonNull
    UUID id;

    @NonNull
    String name;

    @NonNull
    String url;

    String logoUrl;

    @OneToMany(mappedBy = "sponsorId", fetch = FetchType.LAZY)
    @NonNull
    Set<SponsorStatPerCurrencyPerProgramReadEntity> perProgramStatsPerCurrency;

    public SponsorResponse toResponse() {
        return new SponsorResponse()
                .id(id)
                .name(name)
                .url(url)
                .logoUrl(logoUrl);
    }

    public SponsorLinkResponse toLinkResponse() {
        return new SponsorLinkResponse()
                .id(id)
                .name(name)
                .logoUrl(logoUrl);
    }
}
