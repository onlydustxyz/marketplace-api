package onlydust.com.marketplace.api.read.entities.program;

import onlydust.com.marketplace.api.contract.model.DetailedTotalMoneyTotalPerCurrencyInner;
import onlydust.com.marketplace.api.read.entities.currency.CurrencyReadEntity;
import org.springframework.lang.Nullable;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static java.math.BigDecimal.ZERO;
import static onlydust.com.marketplace.kernel.mapper.AmountMapper.pretty;
import static onlydust.com.marketplace.kernel.mapper.AmountMapper.prettyUsd;

public interface ProgramTransactionStat {
    CurrencyReadEntity currency();

    default @Nullable DetailedTotalMoneyTotalPerCurrencyInner toMoney(BigDecimal amount, BigDecimal usdTotal) {
        if (currency() == null)
            return null;

        final var usdQuote = currency().latestUsdQuote() == null ? null : currency().latestUsdQuote().getPrice();
        final var usdAmount = usdAmount(amount);
        final var ratio = (usdTotal == null || usdAmount == null || usdTotal.compareTo(ZERO) == 0) ? null :
                usdAmount.multiply(BigDecimal.valueOf(100)).divide(usdTotal, 0, RoundingMode.HALF_EVEN);

        return new DetailedTotalMoneyTotalPerCurrencyInner()
                .amount(amount)
                .currency(currency().toShortResponse())
                .prettyAmount(pretty(amount, currency().decimals(), usdQuote))
                .usdEquivalent(prettyUsd(usdAmount))
                .usdConversionRate(usdQuote)
                .ratio(ratio);
    }

    default @Nullable BigDecimal usdAmount(BigDecimal amount) {
        return currency() == null || currency().latestUsdQuote() == null ? null : currency().latestUsdQuote().getPrice().multiply(amount);
    }
}
