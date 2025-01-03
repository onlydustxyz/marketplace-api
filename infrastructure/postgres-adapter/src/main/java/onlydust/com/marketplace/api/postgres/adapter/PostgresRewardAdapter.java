package onlydust.com.marketplace.api.postgres.adapter;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.model.Payment;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.BillingProfile;
import onlydust.com.marketplace.accounting.domain.model.user.GithubUserId;
import onlydust.com.marketplace.accounting.domain.port.out.AccountingRewardStoragePort;
import onlydust.com.marketplace.accounting.domain.view.EarningsView;
import onlydust.com.marketplace.accounting.domain.view.RewardDetailsView;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.ShortRewardQueryEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.backoffice.BoEarningsQueryEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.backoffice.BoRewardQueryEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.BatchPaymentEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.NodeGuardianBoostRewardEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.RewardEntity;
import onlydust.com.marketplace.api.postgres.adapter.mapper.ProjectMapper;
import onlydust.com.marketplace.api.postgres.adapter.repository.*;
import onlydust.com.marketplace.api.postgres.adapter.repository.backoffice.BatchPaymentRepository;
import onlydust.com.marketplace.kernel.model.*;
import onlydust.com.marketplace.project.domain.model.Project;
import onlydust.com.marketplace.project.domain.model.Reward;
import onlydust.com.marketplace.project.domain.port.output.BoostedRewardStoragePort;
import onlydust.com.marketplace.project.domain.port.output.RewardStoragePort;
import onlydust.com.marketplace.project.domain.view.ShortProjectRewardView;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@AllArgsConstructor
public class PostgresRewardAdapter implements RewardStoragePort, AccountingRewardStoragePort, BoostedRewardStoragePort {
    private final ShortProjectViewEntityRepository shortProjectViewEntityRepository;
    private final BatchPaymentRepository batchPaymentRepository;
    private final BackofficeRewardViewRepository backofficeRewardViewRepository;
    private final RewardRepository rewardRepository;
    private final ShortRewardViewRepository shortRewardViewRepository;
    private final BackofficeEarningsViewRepository backofficeEarningsViewRepository;
    private final NodeGuardianBoostRewardRepository nodeGuardianBoostRewardRepository;

    @Override
    @Transactional
    public void save(Reward reward) {
        rewardRepository.saveAndFlush(RewardEntity.of(reward));
    }

    @Override
    @Transactional
    public void delete(RewardId rewardId) {
        rewardRepository.deleteById(rewardId.value());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Reward> get(RewardId rewardId) {
        return rewardRepository.findById(rewardId.value()).map(RewardEntity::toReward);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Project> listProjectsByRecipient(Long githubUserId) {
        return shortProjectViewEntityRepository.listProjectsByRewardRecipient(githubUserId)
                .stream()
                .map(ProjectMapper::mapShortProjectViewToProject)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Payment> findPayment(Payment.Id batchPaymentId) {
        return batchPaymentRepository.findById(batchPaymentId.value()).map(BatchPaymentEntity::toDomain);
    }

    @Override
    @Transactional
    public void savePayment(Payment payment) {
        batchPaymentRepository.saveAndFlush(BatchPaymentEntity.fromDomain(payment));
    }

    @Override
    public EarningsView getEarnings(@NonNull Set<RewardStatus.Input> statuses,
                                    @NonNull List<GithubUserId> recipientIds,
                                    @NonNull List<BillingProfile.Id> billingProfileIds,
                                    @NonNull List<ProjectId> projectIds,
                                    Date fromRequestedAt, Date toRequestedAt,
                                    Date fromProcessedAt, Date toProcessedAt) {
        return new EarningsView(
                backofficeEarningsViewRepository.getEarnings(
                        statuses.stream().map(Enum::name).toList(),
                        recipientIds.stream().map(GithubUserId::value).toList(),
                        billingProfileIds.stream().map(BillingProfile.Id::value).toList(),
                        projectIds.stream().map(ProjectId::value).toList(),
                        fromRequestedAt, toRequestedAt,
                        fromProcessedAt, toProcessedAt
                ).stream().map(BoEarningsQueryEntity::toDomain).toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<RewardDetailsView> findPaidRewardsToNotify() {
        return backofficeRewardViewRepository.findPaidRewardsToNotify().stream().map(BoRewardQueryEntity::toDomain).toList();
    }

    @Override
    @Transactional
    public void markRewardsAsPaymentNotified(List<RewardId> rewardIds) {
        rewardRepository.markRewardAsPaymentNotified(rewardIds.stream().map(UuidWrapper::value).toList());
    }

    @Override
    @Transactional
    public void saveAll(List<Payment> payments) {
        batchPaymentRepository.saveAllAndFlush(payments.stream().map(BatchPaymentEntity::fromDomain).toList());
    }

    @Override
    public void deletePayment(Payment.Id paymentId) {
        batchPaymentRepository.deleteById(paymentId.value());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<RewardDetailsView> getReward(RewardId id) {
        return backofficeRewardViewRepository.findAllByRewardIds(List.of(id.value())).stream().findFirst().map(BoRewardQueryEntity::toDomain);
    }

    @Override
    public void updateBillingProfileFromRecipientPayoutPreferences(RewardId rewardId) {
        rewardRepository.updateBillingProfileFromRecipientPayoutPreferences(rewardId.value());
    }

    @Override
    @Transactional
    public void markRewardsAsBoosted(List<RewardId> rewardsBoosted, Long recipientId) {
        for (final var rewardBoostedId : rewardsBoosted) {
            nodeGuardianBoostRewardRepository.save(new NodeGuardianBoostRewardEntity(rewardBoostedId.value(), recipientId, null));
        }
    }

    @Override
    @Transactional
    public void updateBoostedRewardsWithBoostRewardId(List<RewardId> rewardsBoosted, Long recipientId, RewardId rewardId) {
        for (final var rewardBoostedId : rewardsBoosted) {
            nodeGuardianBoostRewardRepository.save(new NodeGuardianBoostRewardEntity(rewardBoostedId.value(), recipientId, rewardId.value()));
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShortProjectRewardView> getRewardsToBoostFromEcosystemNotLinkedToProject(UUID ecosystemId, ProjectId projectId) {
        return shortRewardViewRepository.findRewardsToBoosWithNodeGuardiansForEcosystemIdNotLinkedToProject(ecosystemId, projectId.value()).stream()
                .map(ShortRewardQueryEntity::toProjectDomain).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Integer> getBoostedRewardsCountByRecipientId(Long recipientId) {
        return shortRewardViewRepository.countNumberOfBoostByRecipientId(recipientId);
    }

    @Override
    @Transactional
    public void updateBillingProfileForRecipientUserIdAndProjectId(BillingProfile.Id billingProfileId, UserId userId, ProjectId projectId) {
        rewardRepository.updateBillingProfileForRecipientUserIdAndProjectId(billingProfileId.value(), userId.value(), projectId.value());
    }

    @Override
    @Transactional
    public List<RewardId> removeBillingProfile(BillingProfile.Id billingProfileId) {
        final var rewardIds = rewardRepository.getRewardIdsToBeRemovedFromBillingProfile(billingProfileId.value()).stream().map(RewardEntity::id).toList();
        rewardRepository.removeBillingProfileIdOf(rewardIds);
        return rewardIds.stream().map(RewardId::of).toList();
    }
}
