package onlydust.com.marketplace.project.domain.view;

import lombok.Builder;
import lombok.Data;
import onlydust.com.marketplace.kernel.model.CurrencyView;

import java.math.BigDecimal;

@Data
@Builder
public class BudgetView {
    BigDecimal remaining;
    BigDecimal initialAmount;
    BigDecimal initialDollarsEquivalent;
    BigDecimal remainingDollarsEquivalent;
    CurrencyView currency;
    BigDecimal dollarsConversionRate;
}
