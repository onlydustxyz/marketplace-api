package onlydust.com.marketplace.accounting.domain.port.out;

import onlydust.com.marketplace.accounting.domain.model.Currency;
import onlydust.com.marketplace.accounting.domain.model.Ledger;

import java.util.Optional;

public interface LedgerStorage {
    Optional<Ledger> get(Currency currency);

    void save(Currency currency, Ledger ledger);
}
