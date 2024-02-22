package onlydust.com.marketplace.accounting.domain.model;

import java.math.BigDecimal;
import java.time.Instant;

public record Quote(Currency.Id base, Currency.Id target, BigDecimal price, Instant timestamp) {
    public BigDecimal convertToBaseCurrency(BigDecimal amount) {
        return amount.multiply(price);
    }
}
