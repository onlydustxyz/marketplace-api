package onlydust.com.marketplace.bff.read.entities.ecosystem;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Value;
import lombok.experimental.Accessors;
import onlydust.com.marketplace.api.contract.model.EcosystemBanner;
import org.hibernate.annotations.Immutable;

import java.net.URI;
import java.util.UUID;

@Entity
@NoArgsConstructor(force = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Value
@Table(name = "ecosystem_banners", schema = "public")
@Immutable
@Accessors(fluent = true)
public class EcosystemBannerReadEntity {
    @Id
    @EqualsAndHashCode.Include
    @NonNull
    private UUID id;

    @Enumerated(EnumType.STRING)
    private EcosystemBanner.FontColorEnum fontColor;
    private String imageUrl;

    public EcosystemBanner toDto() {
        return new EcosystemBanner()
                .fontColor(fontColor)
                .url(URI.create(imageUrl))
                ;
    }
}
