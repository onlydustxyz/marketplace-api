package onlydust.com.marketplace.accounting.domain.stubs;

import onlydust.com.marketplace.accounting.domain.model.Ledger;
import onlydust.com.marketplace.accounting.domain.port.out.LedgerStorage;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class LedgerStorageStub implements LedgerStorage {
    private final List<Ledger> ledgers = new ArrayList<>();

    public void save(Ledger ledger) {
        ledgers.add(ledger);
    }

    @Override
    public Optional<Ledger> get(Ledger.Id id) {
        return ledgers.stream().filter(l -> l.id().equals(id)).findFirst();
    }
}
