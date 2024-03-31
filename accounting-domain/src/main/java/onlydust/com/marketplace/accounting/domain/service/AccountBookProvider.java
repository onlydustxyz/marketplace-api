package onlydust.com.marketplace.accounting.domain.service;

import lombok.RequiredArgsConstructor;
import onlydust.com.marketplace.accounting.domain.model.Currency;
import onlydust.com.marketplace.accounting.domain.model.accountbook.AccountBookAggregate;
import onlydust.com.marketplace.accounting.domain.port.out.AccountBookEventStorage;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
public class AccountBookProvider {
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
        accountBookEventStorage.save(currency, accountBook.getAndClearPendingEvents());
    }
}
