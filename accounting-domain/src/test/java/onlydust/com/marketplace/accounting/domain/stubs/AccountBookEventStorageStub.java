package onlydust.com.marketplace.accounting.domain.stubs;

import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.model.Currency;
import onlydust.com.marketplace.accounting.domain.model.accountbook.AccountBookAggregate;
import onlydust.com.marketplace.accounting.domain.model.accountbook.IdentifiedAccountBookEvent;
import onlydust.com.marketplace.accounting.domain.port.out.AccountBookEventStorage;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.support.TransactionSynchronization;

import java.util.*;

import static org.springframework.transaction.support.TransactionSynchronizationManager.registerSynchronization;

public class AccountBookEventStorageStub implements AccountBookEventStorage {
    public final Map<Currency, List<IdentifiedAccountBookEvent>> events = new HashMap<>();

    @NonNull
    @Override
    public synchronized List<IdentifiedAccountBookEvent> getAll(Currency currency) {
        return events.getOrDefault(currency, new ArrayList<>());
    }

    @NonNull
    @Override
    public synchronized List<IdentifiedAccountBookEvent> getSince(Currency currency, long eventId) {
        return getAll(currency).stream().dropWhile(event -> event.id() < eventId).toList();
    }

    @Override
    public synchronized void insert(final @NonNull AccountBookAggregate.Id accountBookId,
                                    final @NonNull Currency currency,
                                    final @NonNull List<IdentifiedAccountBookEvent> pendingEvents) {
        final var events = new ArrayList<>(getAll(currency));
        long eventId = events.isEmpty() ? 0 : events.get(events.size() - 1).id();
        for (var event : pendingEvents) {
            if (event.id() != ++eventId)
                throw new DataIntegrityViolationException("Expected event id to be %d, but got %d".formatted(eventId, event.id()));
        }
        events.addAll(pendingEvents);
        this.events.put(currency, events);

        registerSynchronization(new TransactionSynchronization() {
            public void afterCompletion(int status) {
                if (status != STATUS_COMMITTED) evictAccountBook(currency);
            }
        });
    }

    private void evictAccountBook(final @NonNull Currency currency) {
        this.events.remove(currency);
    }

    @NonNull
    @Override
    public synchronized Optional<Long> getLastEventId(@NonNull Currency currency) {
        final var events = getAll(currency);
        return events.isEmpty() ? Optional.empty() : Optional.of(events.get(events.size() - 1).id());
    }
}
