package onlydust.com.marketplace.accounting.domain.port.out;

import onlydust.com.marketplace.accounting.domain.model.Currency;
import onlydust.com.marketplace.accounting.domain.model.Quote;

import java.util.List;
import java.util.Set;

public interface QuoteService {
    List<Quote> currentPrice(Set<Currency> currencies, Set<Currency> bases);
}
