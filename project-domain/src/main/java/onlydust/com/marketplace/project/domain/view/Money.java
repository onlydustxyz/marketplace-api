package onlydust.com.marketplace.project.domain.view;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;
import onlydust.com.marketplace.kernel.model.CurrencyView;

import java.math.BigDecimal;
import java.util.Optional;

import static onlydust.com.marketplace.kernel.mapper.AmountMapper.pretty;

@Data
@Getter(AccessLevel.NONE)
@Accessors(fluent = true, chain = true)
public class Money {
    @NonNull
    BigDecimal amount;
    @NonNull
    CurrencyView currency;
    BigDecimal usdConversionRateValue;
    BigDecimal dollarsEquivalentValue;

    public Money(@NonNull BigDecimal amount, @NonNull CurrencyView currency) {
        this.amount = amount;
        this.currency = currency;
        this.usdConversionRateValue = null;
        this.dollarsEquivalentValue = null;
    }

    public BigDecimal amount() {
        return amount;
    }

    public CurrencyView currency() {
        return currency;
    }

    public Optional<BigDecimal> usdConversionRate() {
        return Optional.ofNullable(usdConversionRateValue);
    }

    public Optional<BigDecimal> dollarsEquivalent() {
        return Optional.ofNullable(dollarsEquivalentValue);
    }

    public BigDecimal prettyAmount() {
        return pretty(amount, currency.decimals(), usdConversionRateValue != null ? usdConversionRateValue : currency.latestUsdQuote());
    }
}
