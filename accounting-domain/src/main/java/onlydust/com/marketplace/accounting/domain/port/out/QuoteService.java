package onlydust.com.marketplace.accounting.domain.port.out;

import onlydust.com.marketplace.accounting.domain.model.Currency;
import onlydust.com.marketplace.accounting.domain.model.Quote;

import java.util.List;
import java.util.Optional;

public interface QuoteService {
    Optional<Quote> currentPrice(Currency currency, Currency base);

    List<Quote> currentPrice(List<Currency> currencies, Currency base);
}
