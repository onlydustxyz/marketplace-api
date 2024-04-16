package onlydust.com.marketplace.api.rest.api.adapter.mapper;

import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.view.MoneyView;
import onlydust.com.marketplace.api.contract.model.BaseMoney;
import onlydust.com.marketplace.api.contract.model.ConvertibleMoney;
import onlydust.com.marketplace.api.contract.model.Money;

import java.math.RoundingMode;

import static onlydust.com.marketplace.api.rest.api.adapter.mapper.RewardMapper.mapCurrency;
import static onlydust.com.marketplace.kernel.mapper.AmountMapper.pretty;

public interface MoneyMapper {
    static Money toMoney(onlydust.com.marketplace.project.domain.view.Money money) {
        return new Money()
                .amount(money.getAmount())
                .prettyAmount(money.getPrettyAmount())
                .currency(mapCurrency(money.getCurrency()))
                .usdEquivalent(money.getUsdEquivalent());
    }

    static Money toMoney(final MoneyView view) {
        if (view == null) {
            return null;
        }
        return new Money()
                .amount(view.amount())
                .prettyAmount(view.prettyAmount())
                .currency(mapCurrency(view.currency()))
                .usdEquivalent(view.dollarsEquivalent().orElse(null));
    }

    static @NonNull Money add(Money left, @NonNull Money right) {
        return left == null ? right : new Money()
                .currency(right.getCurrency())
                .amount(left.getAmount().add(right.getAmount()))
                .prettyAmount(left.getPrettyAmount().add(right.getPrettyAmount()))
                .usdEquivalent(left.getUsdEquivalent() == null ? null : left.getUsdEquivalent().add(right.getUsdEquivalent()))
                ;
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
