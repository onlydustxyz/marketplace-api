package onlydust.com.marketplace.api.postgres.adapter;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.accounting.domain.model.BillingProfileId;
import onlydust.com.marketplace.accounting.domain.model.RewardId;
import onlydust.com.marketplace.accounting.domain.model.RewardStatus;
import onlydust.com.marketplace.accounting.domain.port.out.RewardStatusStorage;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.RewardStatusEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.RewardStatusRepository;

import java.util.List;
import java.util.Optional;

@AllArgsConstructor
public class PostgresRewardStatusAdapter implements RewardStatusStorage {
    private final RewardStatusRepository rewardStatusRepository;

    @Override
    public void save(RewardStatus rewardStatus) {
        rewardStatusRepository.save(RewardStatusEntity.of(rewardStatus));
    }

    @Override
    public Optional<RewardStatus> get(RewardId rewardId) {
        return rewardStatusRepository.findById(rewardId.value())
                .map(RewardStatusEntity::toRewardStatus);
    }

    @Override
    public void delete(RewardId rewardId) {
        rewardStatusRepository.deleteById(rewardId.value());
    }

    @Override
    public List<RewardStatus> notPaid() {
        return rewardStatusRepository.findByPaidAtIsNull()
                .stream()
                .map(RewardStatusEntity::toRewardStatus)
                .toList();
    }

    @Override
    public List<RewardStatus> notPaid(BillingProfileId billingProfileId) {
        return rewardStatusRepository.findNotPaidByBillingProfile(billingProfileId.value())
                .stream()
                .map(RewardStatusEntity::toRewardStatus)
                .toList();
    }
}
