package onlydust.com.marketplace.accounting.domain.port.out;

import onlydust.com.marketplace.accounting.domain.model.Currency;
import onlydust.com.marketplace.accounting.domain.model.accountbook.IdentifiedAccountBookEvent;

import java.util.List;
import java.util.Optional;

public interface AccountBookEventStorage {
    List<IdentifiedAccountBookEvent> getAll(Currency currency);

    List<IdentifiedAccountBookEvent> getSince(Currency currency, long eventId);

    void save(Currency currency, List<IdentifiedAccountBookEvent> pendingEvents);

    Optional<Long> getLastEventId(Currency currency);
}
