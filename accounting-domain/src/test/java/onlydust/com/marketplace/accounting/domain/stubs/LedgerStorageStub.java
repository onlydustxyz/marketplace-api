package onlydust.com.marketplace.accounting.domain.stubs;

import lombok.SneakyThrows;
import onlydust.com.marketplace.accounting.domain.model.Currency;
import onlydust.com.marketplace.accounting.domain.model.Ledger;
import onlydust.com.marketplace.accounting.domain.port.out.LedgerStorage;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class LedgerStorageStub implements LedgerStorage {
    private final Map<Currency, Ledger> ledgers = new HashMap<>();

    @SneakyThrows
    @Override
    public void save(Currency currency, Ledger ledger) {
        ledgers.put(currency, Ledger.of(ledger));
    }

    @Override
    public Optional<Ledger> get(Currency currency) {
        return Optional.ofNullable(ledgers.get(currency));
    }
}
