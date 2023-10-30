package onlydust.com.marketplace.api.domain.view;

import lombok.Builder;
import lombok.Data;
import onlydust.com.marketplace.api.domain.model.Currency;

import java.math.BigDecimal;

@Data
@Builder
public class BudgetView {
    BigDecimal remaining;
    BigDecimal initialAmount;
    BigDecimal initialDollarsEquivalent;
    BigDecimal remainingDollarsEquivalent;
    Currency currency;
    BigDecimal dollarsConversionRate;

    public BigDecimal getInitialDollarsEquivalent() {
        return currency.equals(Currency.Usd) ? initialAmount : initialDollarsEquivalent;
    }

    public BigDecimal getRemainingDollarsEquivalent() {
        return currency.equals(Currency.Usd) ? remaining : remainingDollarsEquivalent;
    }

}
