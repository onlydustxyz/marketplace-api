package onlydust.com.marketplace.api.postgres.adapter.entity.write;

import lombok.*;
import onlydust.com.marketplace.accounting.domain.model.Currency;
import onlydust.com.marketplace.accounting.domain.model.Quote;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "historical_quotes", schema = "accounting")
@Builder(toBuilder = true)
@NoArgsConstructor(force = true)
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@IdClass(HistoricalQuoteEntity.PrimaryKey.class)
@ToString
public class HistoricalQuoteEntity {
    @Id
    @EqualsAndHashCode.Include
    private @NonNull Instant timestamp;
    @Id
    @EqualsAndHashCode.Include
    private @NonNull UUID currencyId;
    @Id
    @EqualsAndHashCode.Include
    private @NonNull UUID baseId;
    @Getter
    private @NonNull BigDecimal price;

    public static HistoricalQuoteEntity of(Quote quote) {
        return HistoricalQuoteEntity.builder()
                .currencyId(quote.currencyId().value())
                .baseId(quote.base().value())
                .price(quote.price())
                .timestamp(quote.timestamp())
                .build();
    }

    public Quote toDomain() {
        return new Quote(
                Currency.Id.of(currencyId),
                Currency.Id.of(baseId),
                price,
                timestamp);
    }

    @EqualsAndHashCode
    @AllArgsConstructor
    @NoArgsConstructor(force = true)
    @Data
    public static class PrimaryKey implements Serializable {
        private final @NonNull Instant timestamp;
        private final @NonNull UUID currencyId;
        private final @NonNull UUID baseId;
    }
}
