package onlydust.com.marketplace.api.domain.view;

import lombok.Builder;
import lombok.Data;
import onlydust.com.marketplace.api.domain.model.Currency;

import java.math.BigDecimal;
import java.util.List;

import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;

@Data
@Builder
public class ProjectBudgetsView {
    List<BudgetView> budgets;

    public BigDecimal getTotalDollarsEquivalent() {
        BigDecimal totalDollarsEquivalent = null;
        for (BudgetView budget : this.budgets) {
            if (budget.getCurrency().equals(Currency.Usd)) {
                totalDollarsEquivalent = ofNullable(totalDollarsEquivalent).orElse(BigDecimal.ZERO);
                totalDollarsEquivalent = totalDollarsEquivalent.add(budget.getTotal());
            } else if (nonNull(budget.getTotalDollarsEquivalent())) {
                totalDollarsEquivalent = ofNullable(totalDollarsEquivalent).orElse(BigDecimal.ZERO);
                totalDollarsEquivalent = totalDollarsEquivalent.add(budget.getTotalDollarsEquivalent());
            }
        }
        return totalDollarsEquivalent;
    }

    public BigDecimal getRemainingDollarsEquivalent() {
        BigDecimal remainingDollarsEquivalent = null;
        for (BudgetView budget : this.budgets) {
            if (budget.getCurrency().equals(Currency.Usd)) {
                remainingDollarsEquivalent = ofNullable(remainingDollarsEquivalent).orElse(BigDecimal.ZERO);
                remainingDollarsEquivalent = remainingDollarsEquivalent.add(budget.getRemaining());
            } else if (nonNull(budget.getRemainingDollarsEquivalent())) {
                remainingDollarsEquivalent = ofNullable(remainingDollarsEquivalent).orElse(BigDecimal.ZERO);
                remainingDollarsEquivalent = remainingDollarsEquivalent.add(budget.getRemainingDollarsEquivalent());
            }
        }
        return remainingDollarsEquivalent;
    }

}
