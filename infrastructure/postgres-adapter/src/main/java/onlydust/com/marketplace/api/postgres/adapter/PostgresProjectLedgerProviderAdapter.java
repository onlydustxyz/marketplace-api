package onlydust.com.marketplace.api.postgres.adapter;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.accounting.domain.model.Currency;
import onlydust.com.marketplace.accounting.domain.model.ProjectId;
import onlydust.com.marketplace.accounting.domain.model.SponsorAccount;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.LedgerEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.LedgerRepository;

import java.util.Optional;

@AllArgsConstructor
public class PostgresProjectLedgerProviderAdapter implements LedgerProvider<ProjectId> {
    private final LedgerRepository ledgerRepository;

    @Override
    public Optional<SponsorAccount> get(ProjectId projectId, Currency currency) {
        return ledgerRepository.findByProjectProjectIdAndCurrencyId(projectId.value(), currency.id().value())
                .map(LedgerEntity::toLedger);
    }
}
