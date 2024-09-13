package onlydust.com.marketplace.api.read.mapper;

import lombok.NonNull;
import onlydust.com.marketplace.api.contract.model.Money;
import onlydust.com.marketplace.api.contract.model.NewMoney;
import onlydust.com.marketplace.api.read.entities.currency.CurrencyReadEntity;

import java.math.BigDecimal;

import static java.util.Objects.isNull;
import static onlydust.com.marketplace.kernel.mapper.AmountMapper.pretty;
import static onlydust.com.marketplace.kernel.mapper.AmountMapper.prettyUsd;

public interface MoneyMapper {
    static NewMoney map(BigDecimal amount, CurrencyReadEntity currency) {
        return new NewMoney(pretty(amount, currency.decimals(), isNull(currency.latestUsdQuote()) ? null : currency.latestUsdQuote().getPrice()),
                currency.toShortResponse());
    }

    static Money toMoney(@NonNull BigDecimal amount, @NonNull CurrencyReadEntity currency) {
        final var usdQuote = currency.latestUsdQuote() == null ? null : currency.latestUsdQuote().getPrice();
        return new Money()
                .amount(amount)
                .prettyAmount(pretty(amount, currency.decimals(), usdQuote))
                .currency(currency.toShortResponse())
                .usdEquivalent(prettyUsd(usdQuote == null ? null : amount.multiply(usdQuote)))
                .usdConversionRate(usdQuote);
    }
}
