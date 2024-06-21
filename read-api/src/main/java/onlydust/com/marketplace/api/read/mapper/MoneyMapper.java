package onlydust.com.marketplace.api.read.mapper;

import onlydust.com.marketplace.api.contract.model.NewMoney;
import onlydust.com.marketplace.api.read.entities.currency.CurrencyReadEntity;

import java.math.BigDecimal;

import static java.util.Objects.isNull;
import static onlydust.com.marketplace.kernel.mapper.AmountMapper.pretty;

public interface MoneyMapper {
    static NewMoney map(BigDecimal amount, CurrencyReadEntity currency) {
        return new NewMoney(pretty(amount, currency.decimals(), isNull(currency.latestUsdQuote()) ? null : currency.latestUsdQuote().getPrice()), currency.toShortResponse());
    }
}
