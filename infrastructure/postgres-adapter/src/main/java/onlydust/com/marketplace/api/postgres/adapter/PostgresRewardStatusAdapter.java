package onlydust.com.marketplace.api.postgres.adapter;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.model.RewardId;
import onlydust.com.marketplace.accounting.domain.model.RewardStatusData;
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
    public void save(RewardStatusData rewardStatusData) {
        rewardStatusRepository.save(RewardStatusDataEntity.of(rewardStatusData));
    }

    @Override
    public Optional<RewardStatusData> get(final @NonNull RewardId rewardId) {
        return rewardStatusRepository.findById(rewardId.value())
                .map(RewardStatusDataEntity::toRewardStatus);
    }

    @Override
    public List<RewardStatusData> get(@NonNull List<RewardId> rewardIds) {
        return rewardStatusRepository.findAllById(rewardIds.stream().map(RewardId::value).toList())
                .stream().map(RewardStatusDataEntity::toRewardStatus).toList();
    }

    @Override
    public void delete(RewardId rewardId) {
        rewardStatusRepository.deleteById(rewardId.value());
    }

    @Override
    public List<RewardStatusData> notRequested() {
        return rewardStatusRepository.findNotRequested()
                .stream()
                .map(RewardStatusDataEntity::toRewardStatus)
                .toList();
    }

    @Override
    public List<RewardStatusData> notRequested(BillingProfile.Id billingProfileId) {
        return rewardStatusRepository.findNotRequestedByBillingProfile(billingProfileId.value())
                .stream()
                .map(RewardStatusDataEntity::toRewardStatus)
                .toList();
    }
}
