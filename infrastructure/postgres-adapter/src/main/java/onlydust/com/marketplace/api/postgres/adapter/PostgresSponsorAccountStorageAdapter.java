package onlydust.com.marketplace.api.postgres.adapter;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.accounting.domain.model.SponsorAccount;
import onlydust.com.marketplace.accounting.domain.port.out.SponsorAccountStorage;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.LedgerEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.LedgerRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.LedgerTransactionRepository;

import java.util.Arrays;
import java.util.Optional;

@AllArgsConstructor
public class PostgresSponsorAccountStorageAdapter implements SponsorAccountStorage {
    private final LedgerRepository ledgerRepository;
    private final LedgerTransactionRepository ledgerTransactionRepository;

    @Override
    public Optional<SponsorAccount> get(SponsorAccount.Id id) {
        return ledgerRepository.findById(id.value()).map(LedgerEntity::toLedger);
    }

    @Override
    public void save(SponsorAccount... sponsorAccounts) {
        ledgerRepository.saveAll(Arrays.stream(sponsorAccounts).map(LedgerEntity::of).toList());
    }

    @Override
    public void delete(SponsorAccount.Transaction.Id transactionId) {
        ledgerTransactionRepository.deleteById(transactionId.value());
    }
}
