package onlydust.com.marketplace.api.postgres.adapter;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.model.RewardId;
import onlydust.com.marketplace.accounting.domain.model.RewardStatus;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.BillingProfile;
import onlydust.com.marketplace.accounting.domain.port.out.RewardStatusStorage;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.RewardStatusDataEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.RewardStatusRepository;

import java.util.List;
import java.util.Optional;

@AllArgsConstructor
public class PostgresRewardStatusAdapter implements RewardStatusStorage {
    private final RewardStatusRepository rewardStatusRepository;

    @Override
    public void save(RewardStatus rewardStatus) {
        rewardStatusRepository.save(RewardStatusDataEntity.of(rewardStatus));
    }

    @Override
    public Optional<RewardStatus> get(final @NonNull RewardId rewardId) {
        return rewardStatusRepository.findById(rewardId.value())
                .map(RewardStatusDataEntity::toRewardStatus);
    }

    @Override
    public void delete(RewardId rewardId) {
        rewardStatusRepository.deleteById(rewardId.value());
    }

    @Override
    public List<RewardStatus> notPaid() {
        return rewardStatusRepository.findByPaidAtIsNull()
                .stream()
                .map(RewardStatusDataEntity::toRewardStatus)
                .toList();
    }

    @Override
    public List<RewardStatus> notPaid(BillingProfile.Id billingProfileId) {
        return rewardStatusRepository.findNotPaidByBillingProfile(billingProfileId.value())
                .stream()
                .map(RewardStatusDataEntity::toRewardStatus)
                .toList();
    }
}
