package onlydust.com.marketplace.api.postgres.adapter;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.accounting.domain.model.ContributorId;
import onlydust.com.marketplace.accounting.domain.model.Currency;
import onlydust.com.marketplace.accounting.domain.model.Ledger;
import onlydust.com.marketplace.accounting.domain.port.out.LedgerProvider;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.LedgerEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.LedgerRepository;

import java.util.Optional;

@AllArgsConstructor
public class PostgresContributorLedgerProviderAdapter implements LedgerProvider<ContributorId> {
    private final LedgerRepository ledgerRepository;

    @Override
    public Optional<Ledger> get(ContributorId contributorId, Currency currency) {
        return ledgerRepository.findByContributorGithubUserIdAndCurrencyId(contributorId.value(), currency.id().value())
                .map(LedgerEntity::toLedger);
    }
}
