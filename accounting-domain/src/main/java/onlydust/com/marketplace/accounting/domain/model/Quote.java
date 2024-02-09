package onlydust.com.marketplace.accounting.domain.model;

import java.math.BigDecimal;
import java.time.Instant;

public record Quote(Currency.Id currencyId, Currency.Id base, BigDecimal price, Instant timestamp) {
}
