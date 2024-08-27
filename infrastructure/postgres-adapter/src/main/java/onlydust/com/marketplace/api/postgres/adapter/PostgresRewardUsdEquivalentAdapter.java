package onlydust.com.marketplace.api.postgres.adapter;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.kernel.model.RewardId;
import onlydust.com.marketplace.accounting.domain.model.RewardUsdEquivalent;
import onlydust.com.marketplace.accounting.domain.port.out.RewardUsdEquivalentStorage;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.RewardUsdEquivalentDataViewEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.RewardUsdEquivalentDataRepository;

import java.util.Optional;

@AllArgsConstructor
public class PostgresRewardUsdEquivalentAdapter implements RewardUsdEquivalentStorage {

    private final RewardUsdEquivalentDataRepository rewardUsdEquivalentDataRepository;

    @Override
    public Optional<RewardUsdEquivalent> get(RewardId rewardId) {
        return rewardUsdEquivalentDataRepository.findById(rewardId.value())
                .map(RewardUsdEquivalentDataViewEntity::toDomain);
    }
}
