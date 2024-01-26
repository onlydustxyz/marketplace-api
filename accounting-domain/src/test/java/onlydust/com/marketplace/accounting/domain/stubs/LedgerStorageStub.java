package onlydust.com.marketplace.accounting.domain.stubs;

import onlydust.com.marketplace.accounting.domain.model.Ledger;
import onlydust.com.marketplace.accounting.domain.port.out.LedgerStorage;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class LedgerStorageStub implements LedgerStorage {
    private final Map<Ledger.Id, Ledger> ledgers = new HashMap<>();

    @Override
    public void save(Ledger ledger) {
        ledgers.put(ledger.id(), ledger);
    }

    @Override
    public Optional<Ledger> get(Ledger.Id id) {
        return Optional.ofNullable(ledgers.get(id));
    }
}
