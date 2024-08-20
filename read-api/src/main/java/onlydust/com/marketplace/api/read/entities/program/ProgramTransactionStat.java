package onlydust.com.marketplace.api.read.entities.program;

import lombok.NonNull;
import onlydust.com.marketplace.api.contract.model.DetailedTotalMoneyTotalPerCurrencyInner;
import onlydust.com.marketplace.api.read.entities.currency.CurrencyReadEntity;
import org.springframework.lang.Nullable;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static java.math.BigDecimal.ZERO;
import static onlydust.com.marketplace.kernel.mapper.AmountMapper.pretty;
import static onlydust.com.marketplace.kernel.mapper.AmountMapper.prettyUsd;

public interface ProgramTransactionStat {
    @NonNull
    CurrencyReadEntity currency();

    default DetailedTotalMoneyTotalPerCurrencyInner toMoney(BigDecimal amount, BigDecimal usdTotal) {
        final var usdQuote = currency().latestUsdQuote() == null ? null : currency().latestUsdQuote().getPrice();
        final var usdAmount = usdAmount(amount);
        final var ratio = (usdTotal == null || usdAmount == null || usdTotal.compareTo(ZERO) == 0) ? null :
                usdAmount.divide(usdTotal, 2, RoundingMode.HALF_EVEN);

        return new DetailedTotalMoneyTotalPerCurrencyInner()
                .amount(amount)
                .currency(currency().toShortResponse())
                .prettyAmount(pretty(amount, currency().decimals(), usdQuote))
                .usdEquivalent(prettyUsd(usdAmount))
                .usdConversionRate(usdQuote)
                .ratio(ratio);
    }

    default @Nullable BigDecimal usdAmount(BigDecimal amount) {
        return currency().latestUsdQuote() == null ? null : currency().latestUsdQuote().getPrice().multiply(amount);
    }
}
