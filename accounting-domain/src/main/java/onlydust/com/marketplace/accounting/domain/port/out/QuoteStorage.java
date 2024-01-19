package onlydust.com.marketplace.accounting.domain.port.out;

import onlydust.com.marketplace.accounting.domain.model.Quote;

import java.util.Collection;

public interface QuoteStorage {
    void save(Collection<Quote> quotes);
}
