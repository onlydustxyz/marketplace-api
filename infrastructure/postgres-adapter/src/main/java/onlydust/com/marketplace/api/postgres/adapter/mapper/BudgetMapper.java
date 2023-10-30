package onlydust.com.marketplace.api.postgres.adapter.mapper;

import onlydust.com.marketplace.api.domain.model.Currency;
import onlydust.com.marketplace.api.domain.view.BudgetView;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.BudgetViewEntity;

public interface BudgetMapper {


    static BudgetView entityToDomain(final BudgetViewEntity entity) {
        return BudgetView.builder()
                .remaining(entity.getRemainingAmount())
                .currency(switch (entity.getCurrency()) {
                    case op -> Currency.Op;
                    case apt -> Currency.Apt;
                    case eth -> Currency.Eth;
                    case usd -> Currency.Usd;
                    case stark -> Currency.Stark;
                })
                .initialAmount(entity.getInitialAmount())
                .remainingDollarsEquivalent(entity.getRemainingAmountDollarsEquivalent())
                .initialDollarsEquivalent(entity.getInitialAmountDollarsEquivalent())
                .dollarsConversionRate(entity.getDollarsConversionRate())
                .build();
    }
}
