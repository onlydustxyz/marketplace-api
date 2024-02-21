package onlydust.com.marketplace.api.postgres.adapter.mapper;

import onlydust.com.marketplace.project.domain.view.BudgetView;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.BudgetViewEntity;

public interface BudgetMapper {


    static BudgetView entityToDomain(final BudgetViewEntity entity) {
        return BudgetView.builder()
                .remaining(entity.getRemainingAmount())
                .currency(entity.getCurrency().toDomain())
                .initialAmount(entity.getInitialAmount())
                .remainingDollarsEquivalent(entity.getRemainingAmountDollarsEquivalent())
                .initialDollarsEquivalent(entity.getInitialAmountDollarsEquivalent())
                .dollarsConversionRate(entity.getDollarsConversionRate())
                .build();
    }
}
