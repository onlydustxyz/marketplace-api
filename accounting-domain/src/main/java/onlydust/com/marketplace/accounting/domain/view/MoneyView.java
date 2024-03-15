package onlydust.com.marketplace.accounting.domain.view;

import lombok.NonNull;
import lombok.Value;
import onlydust.com.marketplace.accounting.domain.model.Currency;

import java.math.BigDecimal;
import java.util.Optional;

@Value
public class MoneyView {
    @NonNull BigDecimal amount;
    @NonNull Currency currency;
    BigDecimal usdConversionRateValue;
    BigDecimal dollarsEquivalentValue;

    public BigDecimal amount() {
        return amount;
    }

    public Currency currency() {
        return currency;
    }

    public Optional<BigDecimal> usdConversionRate() {
        return Optional.ofNullable(usdConversionRateValue);
    }

    public Optional<BigDecimal> dollarsEquivalent() {
        return Optional.ofNullable(dollarsEquivalentValue);
    }
}
