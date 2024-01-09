package onlydust.com.marketplace.accounting.domain.port.out;

import onlydust.com.marketplace.accounting.domain.model.Currency;
import onlydust.com.marketplace.accounting.domain.model.ERC20;
import onlydust.com.marketplace.accounting.domain.model.Quote;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface QuoteService {
    Optional<Quote> currentPrice(ERC20 token, Currency.Code base);
    List<Optional<Quote>> currentPrice(List<Currency.Id> code, Currency.Code base);
}
