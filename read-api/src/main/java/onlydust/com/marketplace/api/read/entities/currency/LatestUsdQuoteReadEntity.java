package onlydust.com.marketplace.api.read.entities.currency;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.Immutable;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "latest_usd_quotes", schema = "accounting")
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
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
