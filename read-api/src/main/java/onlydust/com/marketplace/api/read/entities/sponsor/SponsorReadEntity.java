package onlydust.com.marketplace.api.read.entities.sponsor;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import onlydust.com.marketplace.api.contract.model.SponsorResponse;
import org.hibernate.annotations.Immutable;

import java.util.UUID;

@Entity
@NoArgsConstructor(force = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Table(name = "sponsors", schema = "public")
@Immutable
@Accessors(fluent = true)
public class SponsorReadEntity {
    @Id
    @EqualsAndHashCode.Include
    @NonNull
    UUID id;

    @NonNull
    String name;

    @NonNull
    String url;

    String logoUrl;

    public SponsorResponse toDto() {
        return new SponsorResponse()
                .id(id)
                .name(name)
                .url(url)
                .logoUrl(logoUrl);
    }
}
