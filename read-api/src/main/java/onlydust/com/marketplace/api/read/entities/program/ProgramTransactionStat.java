package onlydust.com.marketplace.api.read.entities.program;

import lombok.NonNull;
import onlydust.com.marketplace.api.contract.model.Money;
import onlydust.com.marketplace.api.read.entities.currency.CurrencyReadEntity;
import org.springframework.lang.Nullable;

import java.math.BigDecimal;

import static onlydust.com.marketplace.kernel.mapper.AmountMapper.pretty;
import static onlydust.com.marketplace.kernel.mapper.AmountMapper.prettyUsd;

public interface ProgramTransactionStat {
    @NonNull
    CurrencyReadEntity currency();

    default Money toMoney(BigDecimal amount) {
        final var usdQuote = currency().latestUsdQuote() == null ? null : currency().latestUsdQuote().getPrice();

        return new Money()
                .amount(amount)
                .currency(currency().toShortResponse())
                .prettyAmount(pretty(amount, currency().decimals(), usdQuote))
                .usdEquivalent(prettyUsd(usdQuote == null ? null : usdQuote.multiply(amount)))
                .usdConversionRate(usdQuote);
    }

    default @Nullable BigDecimal usdAmount(BigDecimal amount) {
        return currency().latestUsdQuote() == null ? null : currency().latestUsdQuote().getPrice().multiply(amount);
    }
}
