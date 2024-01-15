package onlydust.com.marketplace.accounting.domain.port.out;

import onlydust.com.marketplace.accounting.domain.model.Currency;
import onlydust.com.marketplace.accounting.domain.model.Quote;

import java.util.List;

public interface QuoteService {
    List<Quote> currentPrice(List<Currency> currencies, Currency base);
}
