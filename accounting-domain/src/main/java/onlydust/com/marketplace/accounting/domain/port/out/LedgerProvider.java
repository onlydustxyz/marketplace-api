package onlydust.com.marketplace.accounting.domain.port.out;

import onlydust.com.marketplace.accounting.domain.model.Currency;
import onlydust.com.marketplace.accounting.domain.model.Ledger;

import java.util.Optional;

public interface LedgerProvider<ID> {
    Optional<Ledger.Id> get(ID ownerId, Currency currency);

    Ledger.Id create(ID ownerId, Currency currency);
}
