package onlydust.com.marketplace.bff.read.entities.currency;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Value;
import org.hibernate.annotations.Immutable;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "latest_usd_quotes", schema = "accounting")
@Value
@NoArgsConstructor(force = true)
@Immutable
public class LatestUsdQuoteReadEntity {
    @Id
    @NonNull
    UUID currencyId;
    @NonNull
    BigDecimal price;
    @NonNull
    Instant timestamp;
}