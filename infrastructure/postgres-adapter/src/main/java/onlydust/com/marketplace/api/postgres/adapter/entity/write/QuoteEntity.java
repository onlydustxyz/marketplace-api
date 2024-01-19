package onlydust.com.marketplace.api.postgres.adapter.entity.write;

import lombok.*;
import onlydust.com.marketplace.accounting.domain.model.Quote;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "quotes", schema = "public")
@Builder(toBuilder = true)
@NoArgsConstructor(force = true)
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@IdClass(QuoteEntity.PrimaryKey.class)
@ToString
public class QuoteEntity {
    @Id
    @EqualsAndHashCode.Include
    private @NonNull UUID currencyId;
    @Id
    @EqualsAndHashCode.Include
    private @NonNull UUID baseId;
    @Getter
    private @NonNull BigDecimal price;

    public static QuoteEntity of(Quote quote) {
        return QuoteEntity.builder()
                .currencyId(quote.currencyId().value())
                .baseId(quote.base().value())
                .price(quote.price())
                .build();
    }

    @EqualsAndHashCode
    @AllArgsConstructor
    @NoArgsConstructor(force = true)
    @Data
    public static class PrimaryKey implements Serializable {
        private final @NonNull UUID currencyId;
        private final @NonNull UUID baseId;
    }
}
