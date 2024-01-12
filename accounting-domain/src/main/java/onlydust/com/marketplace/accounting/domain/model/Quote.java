package onlydust.com.marketplace.accounting.domain.model;

import java.math.BigDecimal;

public record Quote(Currency.Id currencyId, Currency.Id base, BigDecimal price) {
}
