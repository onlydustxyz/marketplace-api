package onlydust.com.marketplace.api.domain.view;

import lombok.Builder;
import lombok.Data;
import onlydust.com.marketplace.api.domain.model.Currency;

import java.math.BigDecimal;

@Data
@Builder
public class BudgetView {
    BigDecimal remaining;
    BigDecimal total;
    BigDecimal totalDollarsEquivalent;
    BigDecimal remainingDollarsEquivalent;
    Currency currency;
}
