package onlydust.com.marketplace.api.postgres.adapter;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.accounting.domain.model.Ledger;
import onlydust.com.marketplace.accounting.domain.port.out.LedgerStorage;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.LedgerEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.LedgerRepository;

import java.util.Arrays;
import java.util.Optional;

@AllArgsConstructor
public class PostgresLedgerStorageAdapter implements LedgerStorage {
    private final LedgerRepository ledgerRepository;

    @Override
    public Optional<Ledger> get(Ledger.Id id) {

        final var ledger = ledgerRepository.findById(id.value()).map(LedgerEntity::toLedger);
        return ledger;
    }

    @Override
    public void save(Ledger... ledgers) {
        ledgerRepository.saveAll(Arrays.stream(ledgers).map(LedgerEntity::of).toList());
    }
}
