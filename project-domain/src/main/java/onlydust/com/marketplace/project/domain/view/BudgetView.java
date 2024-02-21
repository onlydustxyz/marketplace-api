package onlydust.com.marketplace.project.domain.view;

import lombok.Builder;
import lombok.Data;
import onlydust.com.marketplace.project.domain.model.Currency;

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
        return currency.equals(Currency.USD) ? initialAmount : initialDollarsEquivalent;
    }

    public BigDecimal getRemainingDollarsEquivalent() {
        return currency.equals(Currency.USD) ? remaining : remainingDollarsEquivalent;
    }

}
