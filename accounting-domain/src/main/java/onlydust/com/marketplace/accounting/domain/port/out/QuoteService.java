package onlydust.com.marketplace.accounting.domain.port.out;

import onlydust.com.marketplace.accounting.domain.model.Currency;
import onlydust.com.marketplace.accounting.domain.model.ERC20;
import onlydust.com.marketplace.accounting.domain.model.Quote;

import java.math.BigDecimal;

public interface QuoteService {
    BigDecimal currentPrice(ERC20 token, Currency.Code base);
}
