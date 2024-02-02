package onlydust.com.marketplace.api.postgres.adapter;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.accounting.domain.model.Currency;
import onlydust.com.marketplace.accounting.domain.model.Ledger;
import onlydust.com.marketplace.accounting.domain.model.SponsorId;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.LedgerEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.LedgerRepository;

import java.util.Optional;

@AllArgsConstructor
public class PostgresSponsorLedgerProviderAdapter implements LedgerProvider<SponsorId> {
    private final LedgerRepository ledgerRepository;

    @Override
    public Optional<Ledger> get(SponsorId sponsorId, Currency currency) {
        return ledgerRepository.findBySponsorSponsorIdAndCurrencyId(sponsorId.value(), currency.id().value())
                .map(LedgerEntity::toLedger);
    }
}
