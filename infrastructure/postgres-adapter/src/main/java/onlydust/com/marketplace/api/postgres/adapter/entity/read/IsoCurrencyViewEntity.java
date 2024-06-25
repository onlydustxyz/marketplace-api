package onlydust.com.marketplace.api.postgres.adapter.entity.read;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.FieldDefaults;
import onlydust.com.marketplace.accounting.domain.model.Currency;
import org.hibernate.annotations.Immutable;

@Entity
@Table(name = "iso_currencies", schema = "rfd")
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Immutable
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor(force = true)
public class IsoCurrencyViewEntity {
    @Id
    @EqualsAndHashCode.Include
    @NonNull
    String alphaCode;
    @NonNull
    String name;
    @NonNull
    Integer numericCode;
    @NonNull
    Integer minorUnit;

    public Currency toCurrency() {
        return Currency.fiat(name, Currency.Code.of(alphaCode), minorUnit);
    }
}
