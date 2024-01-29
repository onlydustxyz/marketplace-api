package onlydust.com.marketplace.accounting.domain.stubs;

import lombok.SneakyThrows;
import onlydust.com.marketplace.accounting.domain.model.Currency;
import onlydust.com.marketplace.accounting.domain.model.Ledger;
import onlydust.com.marketplace.accounting.domain.port.out.LedgerProvider;
import onlydust.com.marketplace.accounting.domain.port.out.LedgerStorage;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class LedgerStorageStub<OwnerId> implements LedgerStorage, LedgerProvider<OwnerId> {
    private static final List<Ledger> ledgers = new ArrayList<>();

    @SneakyThrows
    @Override
    public void save(Ledger... ledgers) {
        LedgerStorageStub.ledgers.removeAll(List.of(ledgers));
        LedgerStorageStub.ledgers.addAll(List.of(ledgers));
    }

    @Override
    public Optional<Ledger> get(Ledger.Id id) {
        return ledgers.stream().filter(l -> l.id().equals(id)).findFirst().map(Ledger::of);
    }

    @Override
    public Optional<Ledger> get(OwnerId ownerId, Currency currency) {
        return ledgers.stream().filter(l -> l.ownerId().equals(ownerId) && l.currency().equals(currency)).findFirst().map(Ledger::of);
    }
}
