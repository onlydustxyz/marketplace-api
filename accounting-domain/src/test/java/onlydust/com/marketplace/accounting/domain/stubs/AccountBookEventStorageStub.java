package onlydust.com.marketplace.accounting.domain.stubs;

import onlydust.com.marketplace.accounting.domain.model.Currency;
import onlydust.com.marketplace.accounting.domain.model.accountbook.AccountBookEvent;
import onlydust.com.marketplace.accounting.domain.port.out.AccountBookEventStorage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AccountBookEventStorageStub implements AccountBookEventStorage {
    public final Map<Currency, List<AccountBookEvent>> events = new HashMap<>();

    @Override
    public List<AccountBookEvent> get(Currency currency) {
        return events.getOrDefault(currency, new ArrayList<>());
    }

    @Override
    public void save(Currency currency, List<AccountBookEvent> pendingEvents) {
        final var events = new ArrayList<>(get(currency));
        events.addAll(pendingEvents);
        this.events.put(currency, events);
    }
}
