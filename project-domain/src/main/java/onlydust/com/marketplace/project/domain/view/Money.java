package onlydust.com.marketplace.project.domain.view;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import onlydust.com.marketplace.kernel.model.CurrencyView;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static onlydust.com.marketplace.kernel.mapper.AmountMapper.pretty;

@Value
@Builder
@AllArgsConstructor
public class Money {
    BigDecimal amount;
    CurrencyView currency;
    BigDecimal usdEquivalent;

    public BigDecimal getPrettyAmount() {
        if (amount == null || currency == null) {
            return null;
        }

        final var rate = (amount.compareTo(BigDecimal.ZERO) == 0 || usdEquivalent == null) ? null : usdEquivalent.divide(amount, RoundingMode.HALF_EVEN);
        return pretty(amount, currency.decimals(), rate);
    }

    public BigDecimal getUsdConversionRate() {
        return (amount.compareTo(BigDecimal.ZERO) == 0 || usdEquivalent == null) ? null : usdEquivalent.divide(amount, RoundingMode.HALF_EVEN);
    }
}
