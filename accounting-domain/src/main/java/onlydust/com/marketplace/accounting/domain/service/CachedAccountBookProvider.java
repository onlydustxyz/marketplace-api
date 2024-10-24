package onlydust.com.marketplace.accounting.domain.service;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import onlydust.com.marketplace.accounting.domain.exception.EventSequenceViolationException;
import onlydust.com.marketplace.accounting.domain.model.Currency;
import onlydust.com.marketplace.accounting.domain.model.accountbook.AccountBookAggregate;
import onlydust.com.marketplace.accounting.domain.model.accountbook.AccountBookObserver;
import onlydust.com.marketplace.accounting.domain.model.accountbook.IdentifiedAccountBookEvent;
import onlydust.com.marketplace.accounting.domain.port.out.AccountBookEventStorage;
import onlydust.com.marketplace.accounting.domain.port.out.AccountBookStorage;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static onlydust.com.marketplace.kernel.exception.OnlyDustException.internalServerError;
import static org.springframework.transaction.support.TransactionSynchronizationManager.registerSynchronization;

@RequiredArgsConstructor
public class CachedAccountBookProvider {
    private final AccountBookEventStorage accountBookEventStorage;
    private final AccountBookStorage accountBookStorage;
    private final AccountBookObserver accountBookObserver;
    private final Map<Currency, AccountBookAggregate> accountBooks = new HashMap<>();

    private AccountBookAggregate getOrDefault(final @NonNull Currency currency) {
        return accountBooks.computeIfAbsent(currency, (c) -> accountBookStorage.get(c.id()).orElseGet(AccountBookAggregate::empty));
    }

    @Transactional(readOnly = true)
    public synchronized AccountBookAggregate get(final @NonNull Currency currency) {
        final var accountBookAggregate = getOrDefault(currency);
        accountBookAggregate.receive(accountBookEventStorage.getSince(currency, accountBookAggregate.nextEventId()));
        registerSynchronization(new TransactionSynchronization() {
            public void afterCompletion(int status) {
                if (status != STATUS_COMMITTED) evictAccountBook(currency);
            }
        });
        return accountBookAggregate.observed(accountBookObserver);
    }

    @Transactional
    public synchronized void save(final @NonNull Currency currency, final @NonNull AccountBookAggregate accountBook) {
        try {
            final var pendingEvents = accountBook.getAndClearPendingEvents();

            if (!pendingEvents.isEmpty()) {
                checkEventIdsSequenceIntegrity(currency, pendingEvents);
                insertEvents(accountBook.id(), currency, pendingEvents);
            }
        } catch (Exception e) {
            evictAccountBook(currency);
            throw internalServerError("Could not save account book", e);
        }
    }

    private void insertEvents(@NonNull AccountBookAggregate.Id accountBookId, @NonNull Currency currency, List<IdentifiedAccountBookEvent> pendingEvents) throws EventSequenceViolationException {
        try {
            accountBookEventStorage.insert(accountBookId, currency, pendingEvents);
        } catch (DataIntegrityViolationException e) {
            throw new EventSequenceViolationException("Failed to insert events", e);
        }
    }

    private void checkEventIdsSequenceIntegrity(final @NonNull Currency currency, final @NonNull List<IdentifiedAccountBookEvent> pendingEvents) throws EventSequenceViolationException {
        long lastPersistedEventId = accountBookEventStorage.getLastEventId(currency).orElse(0L);
        for (final var pendingEvent : pendingEvents) {
            if (pendingEvent.id() != ++lastPersistedEventId) {
                throw new EventSequenceViolationException("Expected next event id to be %d but got %d. Event ids must be strictly sequential."
                        .formatted(lastPersistedEventId, pendingEvent.id()));
            }
        }
    }

    private synchronized void evictAccountBook(@NonNull Currency currency) {
        accountBooks.remove(currency);
    }

    public synchronized void evictAll() {
        accountBooks.clear();
    }
}
