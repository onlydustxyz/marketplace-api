package onlydust.com.marketplace.api.postgres.adapter.entity.write;

import lombok.*;
import lombok.experimental.Accessors;
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
@Table(name = "latest_quotes", schema = "accounting")
@Builder(toBuilder = true)
@NoArgsConstructor(force = true)
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Accessors(fluent = true)
@IdClass(LatestQuoteEntity.PrimaryKey.class)
@ToString
public class LatestQuoteEntity {
    @Id
    @EqualsAndHashCode.Include
    private @NonNull UUID baseId;
    @Id
    @EqualsAndHashCode.Include
    private @NonNull UUID targetId;
    @Getter
    private @NonNull BigDecimal price;
    @Getter
    private @NonNull Instant timestamp;

    public static LatestQuoteEntity of(Quote quote) {
        return LatestQuoteEntity.builder()
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
        private final @NonNull UUID baseId;
        private final @NonNull UUID targetId;
    }
}
