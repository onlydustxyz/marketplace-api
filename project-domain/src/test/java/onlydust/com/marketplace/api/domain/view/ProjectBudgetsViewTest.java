package onlydust.com.marketplace.project.domain.view;

import onlydust.com.marketplace.project.domain.model.Currency;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static java.util.Objects.isNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class ProjectBudgetsViewTest {

    @Test
    void should_return_dollars_equivalents_given_only_dollars() {
        // Given
        final BudgetView budget1 = budgetStub(Currency.USD, 1000, 500, null, null);
        final BudgetView budget2 = budgetStub(Currency.USD, 3000, 1520, null, null);
        final ProjectBudgetsView projectBudgetsView = ProjectBudgetsView.builder()
                .budgets(List.of(
                        budget1,
                        budget2
                ))
                .build();

        // Then
        assertEquals(BigDecimal.valueOf(4000.0), projectBudgetsView.getInitialDollarsEquivalent());
        assertEquals(BigDecimal.valueOf(2020.0), projectBudgetsView.getRemainingDollarsEquivalent());
    }

    @Test
    void should_return_dollars_equivalents_given_multiple_currencies() {
        // Given
        final BudgetView budget1 = budgetStub(Currency.USD, 1000, 500, null, null);
        final BudgetView budget2 = budgetStub(Currency.ETH, 1000, 500, 15000D, 1500D);
        final ProjectBudgetsView projectBudgetsView = ProjectBudgetsView.builder()
                .budgets(List.of(
                        budget1,
                        budget2
                ))
                .build();

        // Then
        assertEquals(BigDecimal.valueOf(16000.0), projectBudgetsView.getInitialDollarsEquivalent());
        assertEquals(BigDecimal.valueOf(2000.0), projectBudgetsView.getRemainingDollarsEquivalent());
    }

    @Test
    void should_return_null_dollars_equivalent_values_given_currencies_without_dollars_equivalent() {
        // Given
        final BudgetView budget1 = budgetStub(Currency.APT, 1000, 500, null, null);
        final BudgetView budget2 = budgetStub(Currency.STRK, 1000, 500, null, null);
        final ProjectBudgetsView projectBudgetsView = ProjectBudgetsView.builder()
                .budgets(List.of(
                        budget1,
                        budget2
                ))
                .build();

        // Then
        assertNull(projectBudgetsView.getInitialDollarsEquivalent());
        assertNull(projectBudgetsView.getRemainingDollarsEquivalent());
    }

    private static BudgetView budgetStub(final Currency currency, final double total, final double remaining,
                                         final Double totalDollarsEquivalent, final Double remainingDollarsEquivalent) {
        return BudgetView.builder()
                .initialAmount(BigDecimal.valueOf(total))
                .remaining(BigDecimal.valueOf(remaining))
                .initialDollarsEquivalent(isNull(totalDollarsEquivalent) ? null :
                        BigDecimal.valueOf(totalDollarsEquivalent))
                .remainingDollarsEquivalent(isNull(remainingDollarsEquivalent) ? null :
                        BigDecimal.valueOf(remainingDollarsEquivalent))
                .currency(currency)
                .build();
    }
}
