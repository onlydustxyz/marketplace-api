package onlydust.com.marketplace.accounting.domain.port.out;

import onlydust.com.marketplace.accounting.domain.model.Quote;

public interface QuoteStorage {
    void save(Quote quote);
}
