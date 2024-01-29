package onlydust.com.marketplace.accounting.domain.port.out;

import onlydust.com.marketplace.accounting.domain.model.Currency;
import onlydust.com.marketplace.accounting.domain.model.accountbook.AccountBookEvent;

import java.util.List;

public interface AccountBookEventStorage {
    List<AccountBookEvent> get(Currency currency);

    void save(Currency currency, List<AccountBookEvent> pendingEvents);
}
