package onlydust.com.marketplace.api.read.entities.ecosystem;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import onlydust.com.marketplace.api.contract.model.EcosystemBanner;
import org.hibernate.annotations.Immutable;

import java.net.URI;
import java.util.UUID;

@Entity
@NoArgsConstructor(force = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
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
