package onlydust.com.marketplace.api.postgres.adapter;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.model.ProjectId;
import onlydust.com.marketplace.accounting.domain.model.RewardId;
import onlydust.com.marketplace.accounting.domain.model.RewardStatusData;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.BillingProfile;
import onlydust.com.marketplace.accounting.domain.model.user.UserId;
import onlydust.com.marketplace.accounting.domain.port.out.RewardStatusStorage;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.RewardStatusDataEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.RewardRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.RewardStatusRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@AllArgsConstructor
public class PostgresRewardStatusAdapter implements RewardStatusStorage {
    private final RewardStatusRepository rewardStatusRepository;
    private final RewardRepository rewardRepository;

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
    public void delete(RewardId rewardId) {
        rewardStatusRepository.deleteById(rewardId.value());
    }

    @Override
    public List<RewardStatusData> notPaid() {
        return rewardStatusRepository.findByPaidAtIsNull()
                .stream()
                .map(RewardStatusDataEntity::toRewardStatus)
                .toList();
    }

    @Override
    public List<RewardStatusData> notPaid(BillingProfile.Id billingProfileId) {
        return rewardStatusRepository.findNotPaidByBillingProfile(billingProfileId.value())
                .stream()
                .map(RewardStatusDataEntity::toRewardStatus)
                .toList();
    }

    @Override
    @Transactional
    public void updateBillingProfileForRecipientUserIdAndProjectId(BillingProfile.Id billingProfileId, UserId userId, ProjectId projectId) {
        rewardRepository.updateBillingProfileForRecipientUserIdAndProjectId(billingProfileId.value(), userId.value(), projectId.value());
    }

    @Override
    @Transactional
    public void enableBillingProfile(BillingProfile.Id billingProfileId) {
        rewardRepository.addBillingProfileId(billingProfileId.value());
    }

    @Override
    @Transactional
    public void removeBillingProfile(BillingProfile.Id billingProfileId) {
        rewardRepository.removeBillingProfileId(billingProfileId.value());
    }

    @Override
    @Transactional
    public void updateBillingProfileFromRecipientPayoutPreferences(RewardId rewardId) {
        rewardRepository.updateBillingProfileFromRecipientPayoutPreferences(rewardId.value());

    }
}
