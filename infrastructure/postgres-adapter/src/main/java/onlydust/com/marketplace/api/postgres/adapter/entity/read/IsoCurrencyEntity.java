package onlydust.com.marketplace.api.postgres.adapter.entity.read;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Value;
import onlydust.com.marketplace.accounting.domain.model.Currency;
import org.hibernate.annotations.Immutable;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "iso_currencies", schema = "rfd")
@Value
@Immutable
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor(force = true)
public class IsoCurrencyEntity {
    @Id
    @EqualsAndHashCode.Include
    @NonNull String alphaCode;
    @NonNull String name;
    @NonNull Integer numericCode;
    @NonNull Integer minorUnit;

    public Currency toCurrency() {
        return Currency.fiat(name, Currency.Code.of(alphaCode), minorUnit);
    }
}
