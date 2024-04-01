package onlydust.com.marketplace.accounting.domain.stubs;

import onlydust.com.marketplace.accounting.domain.model.Currency;
import onlydust.com.marketplace.accounting.domain.model.accountbook.IdentifiedAccountBookEvent;
import onlydust.com.marketplace.accounting.domain.port.out.AccountBookEventStorage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AccountBookEventStorageStub implements AccountBookEventStorage {
    public final Map<Currency, List<IdentifiedAccountBookEvent>> events = new HashMap<>();

    @Override
    public synchronized List<IdentifiedAccountBookEvent> getAll(Currency currency) {
        return events.getOrDefault(currency, new ArrayList<>());
    }

    @Override
    public synchronized List<IdentifiedAccountBookEvent> getSince(Currency currency, long eventId) {
        return getAll(currency).stream().dropWhile(event -> event.id() < eventId).toList();
    }

    @Override
    public synchronized void save(Currency currency, List<IdentifiedAccountBookEvent> pendingEvents) {
        final var events = new ArrayList<>(getAll(currency));
        long eventId = events.isEmpty() ? 0 : events.get(events.size() - 1).id();
        for (var event : pendingEvents) {
            if (event.id() != ++eventId)
                throw new IllegalStateException("Expected event id to be %d, but got %d".formatted(eventId, event.id()));
        }
        events.addAll(pendingEvents);
        this.events.put(currency, events);
    }
}
