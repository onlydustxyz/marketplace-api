package onlydust.com.marketplace.api.rest.api.adapter.mapper;

import onlydust.com.marketplace.accounting.domain.model.Currency;
import onlydust.com.marketplace.accounting.domain.view.MoneyView;
import onlydust.com.marketplace.api.contract.model.BaseMoney;
import onlydust.com.marketplace.api.contract.model.ConvertibleMoney;
import onlydust.com.marketplace.api.contract.model.DetailedTotalMoneyTotalPerCurrencyInner;
import onlydust.com.marketplace.api.contract.model.Money;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static onlydust.com.marketplace.api.rest.api.adapter.mapper.RewardMapper.mapCurrency;
import static onlydust.com.marketplace.kernel.mapper.AmountMapper.pretty;
import static onlydust.com.marketplace.kernel.mapper.AmountMapper.prettyUsd;

public interface MoneyMapper {
    static DetailedTotalMoneyTotalPerCurrencyInner toDetailedTotalMoneyTotalPerCurrencyInner(onlydust.com.marketplace.project.domain.view.Money money,
                                                                                             BigDecimal usdTotal) {
        final var ratio = (usdTotal == null || money.dollarsEquivalent().isEmpty() || usdTotal.compareTo(BigDecimal.ZERO) == 0) ? null :
                money.dollarsEquivalent().get().multiply(BigDecimal.valueOf(100)).divide(usdTotal, 0, RoundingMode.HALF_EVEN);

        return new DetailedTotalMoneyTotalPerCurrencyInner()
                .amount(money.amount())
                .prettyAmount(money.prettyAmount())
                .currency(mapCurrency(money.currency()))
                .usdEquivalent(prettyUsd(money.dollarsEquivalent().orElse(null)))
                .usdConversionRate(money.usdConversionRate().orElse(null))
                .ratio(ratio);
    }

    static Money toMoney(onlydust.com.marketplace.project.domain.view.Money money) {
        return new Money()
                .amount(money.amount())
                .prettyAmount(money.prettyAmount())
                .currency(mapCurrency(money.currency()))
                .usdEquivalent(prettyUsd(money.dollarsEquivalent().orElse(null)))
                .usdConversionRate(money.usdConversionRate().orElse(null));
    }

    static Money toMoney(final MoneyView view) {
        if (view == null) {
            return null;
        }
        return new Money()
                .amount(view.amount())
                .prettyAmount(view.prettyAmount())
                .currency(mapCurrency(view.currency()))
                .usdEquivalent(prettyUsd(view.dollarsEquivalent().orElse(null)))
                .usdConversionRate(view.usdConversionRate().orElse(null));
    }

    static Money toMoney(final BigDecimal amount, final Currency currency) {
        if (amount == null || currency == null) {
            return null;
        }
        return toMoney(new MoneyView(amount, currency));
    }

    static ConvertibleMoney toConvertibleMoney(onlydust.com.marketplace.accounting.domain.model.Money money,
                                               onlydust.com.marketplace.accounting.domain.model.Money base) {
        final var conversionRate = base.getValue().divide(money.getValue(), 2, RoundingMode.HALF_EVEN);
        return new ConvertibleMoney()
                .amount(money.getValue())
                .prettyAmount(pretty(money.getValue(), money.getCurrency().decimals(), conversionRate))
                .currency(mapCurrency(money.getCurrency()))
                .target(new BaseMoney()
                        .amount(base.getValue())
                        .currency(mapCurrency(base.getCurrency()))
                        .conversionRate(conversionRate)
                );
    }
}
