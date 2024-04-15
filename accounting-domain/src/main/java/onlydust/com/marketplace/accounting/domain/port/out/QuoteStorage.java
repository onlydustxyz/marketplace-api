package onlydust.com.marketplace.accounting.domain.port.out;

import onlydust.com.marketplace.accounting.domain.model.Currency;
import onlydust.com.marketplace.accounting.domain.model.Quote;

import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Optional;

public interface QuoteStorage {
    void save(Collection<Quote> quotes);

    Optional<Quote> nearest(Currency.Id baseId, Currency.Id targetId, ZonedDateTime date);

    Optional<Quote> latest(Currency.Id baseId, Currency.Id targetId);
}
