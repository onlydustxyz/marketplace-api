package onlydust.com.marketplace.accounting.domain.stubs;

import onlydust.com.marketplace.accounting.domain.model.Currency;
import onlydust.com.marketplace.accounting.domain.model.Ledger;
import onlydust.com.marketplace.accounting.domain.port.out.LedgerProvider;
import org.assertj.core.groups.Tuple;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class LedgerProviderStub<T> implements LedgerProvider<T> {
    private final Map<Tuple, Ledger> ledgers = new HashMap<>();

    @Override
    public Optional<Ledger> get(T ownerId, Currency currency) {
        return Optional.ofNullable(ledgers.get(Tuple.tuple(ownerId, currency)));
    }

    @Override
    public Ledger create(T ownerId, Currency currency) {
        final var ledger = new Ledger();
        if (ledgers.put(Tuple.tuple(ownerId, currency), ledger) != null)
            throw new IllegalStateException("Ledger already exists");
        return ledger;
    }
}
