package onlydust.com.marketplace.accounting.domain.service;

import lombok.RequiredArgsConstructor;
import onlydust.com.marketplace.accounting.domain.model.Currency;
import onlydust.com.marketplace.accounting.domain.model.accountbook.AccountBookAggregate;
import onlydust.com.marketplace.accounting.domain.port.out.AccountBookEventStorage;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
public class CachedAccountBookProvider {
    private final AccountBookEventStorage accountBookEventStorage;
    private final Map<Currency, AccountBookAggregate> accountBooks = new HashMap<>();

    private AccountBookAggregate getOrDefault(Currency currency) {
        return accountBooks.computeIfAbsent(currency, (c) -> AccountBookAggregate.empty());
    }

    @Transactional(readOnly = true)
    public synchronized AccountBookAggregate get(Currency currency) {
        final var accountBookAggregate = getOrDefault(currency);
        accountBookAggregate.receive(accountBookEventStorage.getSince(currency, accountBookAggregate.nextEventId()));
        return accountBookAggregate;
    }

    @Transactional
    public synchronized void save(Currency currency, AccountBookAggregate accountBook) {
        try {
            final Optional<Long> lastPersistedEventId = accountBookEventStorage.getLastEventId(currency);
            final var pendingEvents = accountBook.getAndClearPendingEvents();
            if (pendingEvents.isEmpty()) {
                return;
            }
            if (lastPersistedEventId.isPresent() && lastPersistedEventId.get() + 1 != pendingEvents.get(0).id()) {
                throw new IllegalStateException("Expected next event id to be %d but got %d"
                        .formatted(lastPersistedEventId.get() + 1, pendingEvents.get(0).id()));
            }
            accountBookEventStorage.save(currency, pendingEvents);
        } catch (Exception e) {
            accountBooks.remove(currency);
            throw e;
        }
    }
}
