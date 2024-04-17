package onlydust.com.marketplace.api.postgres.adapter.entity.write;

import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Value;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "latest_usd_quotes", schema = "accounting")
@Value
@NoArgsConstructor(force = true)
public class LatestUsdQuoteEntity {
    @Id
    @NonNull
    UUID currencyId;
    @NonNull
    BigDecimal price;
    @NonNull
    Instant timestamp;
}
