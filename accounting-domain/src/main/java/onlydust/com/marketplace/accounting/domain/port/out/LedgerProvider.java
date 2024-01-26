package onlydust.com.marketplace.accounting.domain.port.out;

import onlydust.com.marketplace.accounting.domain.model.Currency;
import onlydust.com.marketplace.accounting.domain.model.Ledger;

import java.util.Optional;

public interface LedgerProvider<ID> {
    Optional<Ledger> get(ID ownerId, Currency currency);

    void save(ID ownerId, Currency currency, Ledger ledger);
}
