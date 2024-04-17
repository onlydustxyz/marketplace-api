package onlydust.com.marketplace.api.postgres.adapter.entity.write;

import lombok.*;
import onlydust.com.marketplace.accounting.domain.model.Currency;
import onlydust.com.marketplace.accounting.domain.model.Quote;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

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
    private @NonNull UUID baseId;
    @Id
    @EqualsAndHashCode.Include
    private @NonNull UUID targetId;
    @Getter
    private @NonNull BigDecimal price;

    public static HistoricalQuoteEntity of(Quote quote) {
        return HistoricalQuoteEntity.builder()
                .baseId(quote.base().value())
                .targetId(quote.target().value())
                .price(quote.price())
                .timestamp(quote.timestamp())
                .build();
    }

    public Quote toDomain() {
        return new Quote(
                Currency.Id.of(baseId),
                Currency.Id.of(targetId),
                price,
                timestamp);
    }

    @EqualsAndHashCode
    @AllArgsConstructor
    @NoArgsConstructor(force = true)
    @Data
    public static class PrimaryKey implements Serializable {
        private final @NonNull Instant timestamp;
        private final @NonNull UUID baseId;
        private final @NonNull UUID targetId;
    }
}
