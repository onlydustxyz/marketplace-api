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
            case Eth -> CurrencyContract.ETH;
            case Apt -> CurrencyContract.APT;
            case Op -> CurrencyContract.OP;
            case Usd -> CurrencyContract.USD;
            case Stark -> CurrencyContract.STARK;
            case Lords -> CurrencyContract.LORDS;
            case Usdc -> CurrencyContract.USDC;
        };
    }

    static Currency mapCurrency(CurrencyContract currency) {
        return switch (currency) {
            case ETH -> Currency.Eth;
            case APT -> Currency.Apt;
            case OP -> Currency.Op;
            case USD -> Currency.Usd;
            case STARK -> Currency.Stark;
            case LORDS -> Currency.Lords;
            case USDC -> Currency.Usdc;
        };
    }
}
