package onlydust.com.marketplace.api.postgres.adapter.entity.write;

import lombok.*;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "latest_usd_quotes", schema = "accounting")
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
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
