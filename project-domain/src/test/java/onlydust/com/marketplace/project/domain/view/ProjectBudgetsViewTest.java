package onlydust.com.marketplace.project.domain.view;

import onlydust.com.marketplace.kernel.model.CurrencyView;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static java.util.Objects.isNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class ProjectBudgetsViewTest {


    final static CurrencyView ETH = CurrencyView.builder().id(CurrencyView.Id.random()).code("ETH").name("Ethereum").decimals(18).build();
    final static CurrencyView USD = CurrencyView.builder().id(CurrencyView.Id.random()).code("USD").name("Dollar").decimals(2).build();
    final static CurrencyView STRK = CurrencyView.builder().id(CurrencyView.Id.random()).code("STRK").name("Starknet Coin").decimals(18).build();
    final static CurrencyView LORDS = CurrencyView.builder().id(CurrencyView.Id.random()).code("LORDS").name("Lords").decimals(18).build();

    @Test
    void should_return_dollars_equivalents_given_only_dollars() {
        // Given
        final BudgetView budget1 = budgetStub(USD, 1000, 500, 1000.0, 500.0);
        final BudgetView budget2 = budgetStub(USD, 3000, 1520, 3000.0, 1520.0);
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
        final BudgetView budget1 = budgetStub(USD, 1000, 500, 1000.0, 500.0);
        final BudgetView budget2 = budgetStub(ETH, 1000, 500, 15000D, 1500D);
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
        final BudgetView budget1 = budgetStub(LORDS, 1000, 500, null, null);
        final BudgetView budget2 = budgetStub(STRK, 1000, 500, null, null);
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

    private static BudgetView budgetStub(final CurrencyView currency, final double total, final double remaining,
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
