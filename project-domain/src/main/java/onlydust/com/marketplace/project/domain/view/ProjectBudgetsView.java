package onlydust.com.marketplace.project.domain.view;

import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;


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

    public List<BudgetView> getBudgets() {
        return this.budgets.stream()
                .filter(budgetView -> budgetView.getRemaining().compareTo(BigDecimal.ZERO) > 0)
                .collect(Collectors.toList());
    }
}
