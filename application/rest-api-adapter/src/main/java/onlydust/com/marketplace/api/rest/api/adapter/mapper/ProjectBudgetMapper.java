package onlydust.com.marketplace.api.rest.api.adapter.mapper;

import onlydust.com.marketplace.api.contract.model.BudgetResponse;
import onlydust.com.marketplace.api.contract.model.CurrencyContract;
import onlydust.com.marketplace.api.contract.model.ProjectBudgetsResponse;
import onlydust.com.marketplace.api.domain.view.BudgetView;
import onlydust.com.marketplace.api.domain.view.ProjectBudgetsView;

public interface ProjectBudgetMapper {

    static ProjectBudgetsResponse mapProjectBudgetsViewToResponse(final ProjectBudgetsView projectBudgetsView) {
        final ProjectBudgetsResponse projectBudgetsResponse = new ProjectBudgetsResponse();
        projectBudgetsResponse.setRemainingDollarsEquivalent(projectBudgetsView.getRemainingDollarsEquivalent());
        projectBudgetsResponse.setInitialDollarsEquivalent(projectBudgetsView.getInitialDollarsEquivalent());
        projectBudgetsResponse.setRemainingDollarsEquivalent(projectBudgetsView.getRemainingDollarsEquivalent());
        for (BudgetView budget : projectBudgetsView.getBudgets()) {
            projectBudgetsResponse.addBudgetsItem(new BudgetResponse()
                    .remaining(budget.getRemaining())
                    .initialAmount(budget.getInitialAmount())
                    .initialDollarsEquivalent(budget.getInitialDollarsEquivalent())
                    .remainingDollarsEquivalent(budget.getRemainingDollarsEquivalent())
                    .currency(switch (budget.getCurrency()) {
                        case Eth -> CurrencyContract.ETH;
                        case Apt -> CurrencyContract.APT;
                        case Op -> CurrencyContract.OP;
                        case Usd -> CurrencyContract.USD;
                        case Stark -> CurrencyContract.STARK;
                    }));
        }
        return projectBudgetsResponse;
    }
}
