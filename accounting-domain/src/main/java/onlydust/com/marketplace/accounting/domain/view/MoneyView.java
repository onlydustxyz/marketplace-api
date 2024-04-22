package onlydust.com.marketplace.accounting.domain.view;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Value;
import onlydust.com.marketplace.accounting.domain.model.Currency;

import java.math.BigDecimal;
import java.util.Optional;

import static onlydust.com.marketplace.kernel.mapper.AmountMapper.pretty;

@Value
@NoArgsConstructor(force = true, access = lombok.AccessLevel.PRIVATE)
@AllArgsConstructor
public class MoneyView {
    @NonNull
    BigDecimal amount;
    @NonNull
    Currency currency;
    BigDecimal usdConversionRateValue;
    BigDecimal dollarsEquivalentValue;

    public MoneyView(BigDecimal amount, Currency currency) {
        this.amount = amount;
        this.currency = currency;
        this.usdConversionRateValue = currency.latestUsdQuote().orElse(null);
        this.dollarsEquivalentValue = amount.multiply(usdConversionRateValue);
    }

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

    public BigDecimal prettyAmount() {
        return pretty(amount, currency.decimals(), usdConversionRateValue);
    }
}
