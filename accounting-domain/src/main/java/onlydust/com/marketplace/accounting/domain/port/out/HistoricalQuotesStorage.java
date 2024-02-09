package onlydust.com.marketplace.accounting.domain.port.out;

import onlydust.com.marketplace.accounting.domain.model.Currency;
import onlydust.com.marketplace.accounting.domain.model.Quote;

import java.time.ZonedDateTime;
import java.util.Optional;

public interface HistoricalQuotesStorage {

    Optional<Quote> nearest(Currency.Id currencyId, Currency.Id baseId, ZonedDateTime date);
}
