package onlydust.com.marketplace.api.postgres.adapter;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.accounting.domain.model.Currency;
import onlydust.com.marketplace.accounting.domain.model.Ledger;
import onlydust.com.marketplace.accounting.domain.model.RewardId;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.LedgerEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.LedgerRepository;

import java.util.Optional;

@AllArgsConstructor
public class PostgresRewardLedgerProviderAdapter implements LedgerProvider<RewardId> {
    private final LedgerRepository ledgerRepository;

    @Override
    public Optional<Ledger> get(RewardId rewardId, Currency currency) {
        return ledgerRepository.findByRewardRewardIdAndCurrencyId(rewardId.value(), currency.id().value())
                .map(LedgerEntity::toLedger);
    }
}
