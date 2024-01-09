package onlydust.com.marketplace.accounting.domain.stubs;

import onlydust.com.marketplace.accounting.domain.model.Currency;
import onlydust.com.marketplace.accounting.domain.model.Quote;

import java.math.BigDecimal;

public interface Quotes {
    Quote USDC_USD = new Quote(Currencies.USDC.id(), Currency.Code.USD, BigDecimal.valueOf(1.01));
    Quote LORDS_USD = new Quote(Currencies.LORDS.id(), Currency.Code.USD, BigDecimal.valueOf(0.35));
}
