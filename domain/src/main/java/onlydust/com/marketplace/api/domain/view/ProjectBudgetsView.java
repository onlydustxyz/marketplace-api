package onlydust.com.marketplace.api.domain.view;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;

@Data
@Builder
public class ProjectBudgetsView {
    List<BudgetView> budgets;

    public BigDecimal getInitialDollarsEquivalent() {
        BigDecimal totalDollarsEquivalent = null;
        for (BudgetView budget : this.budgets) {
            if (nonNull(budget.getInitialDollarsEquivalent())) {
                totalDollarsEquivalent = ofNullable(totalDollarsEquivalent).orElse(BigDecimal.ZERO);
                totalDollarsEquivalent = totalDollarsEquivalent.add(budget.getInitialDollarsEquivalent());
            }
        }
        return totalDollarsEquivalent;
    }

    public BigDecimal getRemainingDollarsEquivalent() {
        BigDecimal remainingDollarsEquivalent = null;
        for (BudgetView budget : this.budgets) {
            if (nonNull(budget.getRemainingDollarsEquivalent())) {
                remainingDollarsEquivalent = ofNullable(remainingDollarsEquivalent).orElse(BigDecimal.ZERO);
                remainingDollarsEquivalent = remainingDollarsEquivalent.add(budget.getRemainingDollarsEquivalent());
            }
        }
        return remainingDollarsEquivalent;
    }

}
