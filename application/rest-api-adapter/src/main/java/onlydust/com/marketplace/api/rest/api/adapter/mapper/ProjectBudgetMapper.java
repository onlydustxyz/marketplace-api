package onlydust.com.marketplace.api.rest.api.adapter.mapper;

import onlydust.com.marketplace.api.contract.model.BudgetResponse;
import onlydust.com.marketplace.api.contract.model.CurrencyContract;
import onlydust.com.marketplace.api.contract.model.ProjectBudgetsResponse;
import onlydust.com.marketplace.api.domain.model.Currency;
import onlydust.com.marketplace.api.domain.view.BudgetView;
import onlydust.com.marketplace.api.domain.view.ProjectBudgetsView;

public interface ProjectBudgetMapper {

    static ProjectBudgetsResponse mapProjectBudgetsViewToResponse(final ProjectBudgetsView projectBudgetsView) {
        final ProjectBudgetsResponse projectBudgetsResponse = new ProjectBudgetsResponse();
        projectBudgetsResponse.setRemainingDollarsEquivalent(projectBudgetsView.getRemainingDollarsEquivalent());
        projectBudgetsResponse.setInitialDollarsEquivalent(projectBudgetsView.getInitialDollarsEquivalent());
        for (BudgetView budget : projectBudgetsView.getBudgets()) {
            projectBudgetsResponse.addBudgetsItem(new BudgetResponse()
                    .remaining(budget.getRemaining())
                    .initialAmount(budget.getInitialAmount())
                    .initialDollarsEquivalent(budget.getInitialDollarsEquivalent())
                    .remainingDollarsEquivalent(budget.getRemainingDollarsEquivalent())
                    .dollarsConversionRate(budget.getDollarsConversionRate())
                    .currency(mapCurrency(budget.getCurrency())));
        }
        return projectBudgetsResponse;
    }

    static CurrencyContract mapCurrency(Currency currency) {
        return switch (currency) {
            case ETH -> CurrencyContract.ETH;
            case APT -> CurrencyContract.APT;
            case OP -> CurrencyContract.OP;
            case USD -> CurrencyContract.USD;
            case STRK -> CurrencyContract.STRK;
            case LORDS -> CurrencyContract.LORDS;
            case USDC -> CurrencyContract.USDC;
        };
    }

    static Currency mapCurrency(CurrencyContract currency) {
        return switch (currency) {
            case ETH -> Currency.ETH;
            case APT -> Currency.APT;
            case OP -> Currency.OP;
            case USD -> Currency.USD;
            case STRK -> Currency.STRK;
            case LORDS -> Currency.LORDS;
            case USDC -> Currency.USDC;
        };
    }
}
