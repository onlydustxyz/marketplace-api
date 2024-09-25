package onlydust.com.marketplace.api.read.entities;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import onlydust.com.marketplace.accounting.domain.model.Country;
import org.hibernate.annotations.Immutable;

@Entity
@NoArgsConstructor(force = true)
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Immutable
@Accessors(fluent = true)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class CountryReadEntity {
    @Id
    @EqualsAndHashCode.Include
    @NonNull
    String iso3;

    public Country country() {
        return Country.fromIso3(iso3);
    }
}
