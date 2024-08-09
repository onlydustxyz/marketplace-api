package onlydust.com.marketplace.api.read.mapper;

import onlydust.com.marketplace.api.contract.model.DetailedTotalMoney;
import onlydust.com.marketplace.api.contract.model.Money;
import onlydust.com.marketplace.api.read.entities.program.ProgramBudgetReadEntity;

import java.math.BigDecimal;
import java.util.List;

import static java.util.Comparator.comparing;

public interface DetailedTotalMoneyMapper {
    static DetailedTotalMoney map(List<ProgramBudgetReadEntity> budgets) {
        return new DetailedTotalMoney()
                .totalPerCurrency(budgets.stream().map(ProgramBudgetReadEntity::toMoney).sorted(comparing(Money::getUsdEquivalent).reversed()).toList())
                .totalUsdEquivalent(budgets.stream().map(ProgramBudgetReadEntity::usdAmount).reduce(BigDecimal::add).orElse(null));
    }
}
