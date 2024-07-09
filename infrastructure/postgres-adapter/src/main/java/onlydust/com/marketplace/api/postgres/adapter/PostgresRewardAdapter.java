package onlydust.com.marketplace.api.postgres.adapter;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.model.Payment;
import onlydust.com.marketplace.accounting.domain.model.ProjectId;
import onlydust.com.marketplace.accounting.domain.model.RewardId;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.BillingProfile;
import onlydust.com.marketplace.accounting.domain.model.user.GithubUserId;
import onlydust.com.marketplace.accounting.domain.model.user.UserId;
import onlydust.com.marketplace.accounting.domain.port.out.AccountingRewardStoragePort;
import onlydust.com.marketplace.accounting.domain.view.EarningsView;
import onlydust.com.marketplace.accounting.domain.view.RewardDetailsView;
import onlydust.com.marketplace.accounting.domain.view.ShortRewardDetailsView;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.ShortRewardQueryEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.backoffice.BoEarningsQueryEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.backoffice.BoRewardQueryEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.BatchPaymentEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.NodeGuardianBoostRewardEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.RewardEntity;
import onlydust.com.marketplace.api.postgres.adapter.mapper.ProjectMapper;
import onlydust.com.marketplace.api.postgres.adapter.repository.*;
import onlydust.com.marketplace.api.postgres.adapter.repository.backoffice.BatchPaymentRepository;
import onlydust.com.marketplace.kernel.model.RewardStatus;
import onlydust.com.marketplace.kernel.model.UuidWrapper;
import onlydust.com.marketplace.kernel.pagination.Page;
import onlydust.com.marketplace.project.domain.model.Project;
import onlydust.com.marketplace.project.domain.model.Reward;
import onlydust.com.marketplace.project.domain.port.output.BoostedRewardStoragePort;
import onlydust.com.marketplace.project.domain.port.output.RewardStoragePort;
import onlydust.com.marketplace.project.domain.view.ShortProjectRewardView;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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
    public void delete(UUID rewardId) {
        rewardRepository.deleteById(rewardId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Reward> get(UUID rewardId) {
        return rewardRepository.findById(rewardId).map(RewardEntity::toReward);
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
    @Transactional
    public Page<RewardDetailsView> findRewards(int pageIndex, int pageSize,
                                               @NonNull Set<RewardStatus.Input> statuses,
                                               @NonNull List<BillingProfile.Id> billingProfileIds,
                                               @NonNull List<GithubUserId> recipients,
                                               Date fromRequestedAt, Date toRequestedAt,
                                               Date fromProcessedAt, Date toProcessedAt) {
        final var page = backofficeRewardViewRepository.findAllByStatusesAndDates(
                statuses.stream().map(Enum::name).toList(),
                billingProfileIds.stream().map(BillingProfile.Id::value).toList(),
                recipients.stream().map(GithubUserId::value).toList(),
                fromRequestedAt, toRequestedAt,
                fromProcessedAt, toProcessedAt,
                PageRequest.of(pageIndex, pageSize, Sort.by("requested_at").descending())
        );

        return Page.<RewardDetailsView>builder()
                .content(page.getContent().stream().map(BoRewardQueryEntity::toDomain).toList())
                .totalItemNumber((int) page.getTotalElements())
                .totalPageNumber(page.getTotalPages())
                .build();
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
    public Optional<ShortRewardDetailsView> getShortReward(RewardId rewardId) {
        return shortRewardViewRepository.findById(rewardId.value()).map(ShortRewardQueryEntity::toAccountingDomain);
    }

    @Override
    public void updateBillingProfileFromRecipientPayoutPreferences(RewardId rewardId) {
        rewardRepository.updateBillingProfileFromRecipientPayoutPreferences(rewardId.value());
    }

    @Override
    @Transactional
    public void markRewardsAsBoosted(List<UUID> rewardsBoosted, Long recipientId) {
        for (UUID rewardBoostedId : rewardsBoosted) {
            nodeGuardianBoostRewardRepository.save(new NodeGuardianBoostRewardEntity(rewardBoostedId, recipientId, null));
        }
    }

    @Override
    @Transactional
    public void updateBoostedRewardsWithBoostRewardId(List<UUID> rewardsBoosted, Long recipientId, UUID rewardId) {
        for (UUID rewardBoostedId : rewardsBoosted) {
            nodeGuardianBoostRewardRepository.save(new NodeGuardianBoostRewardEntity(rewardBoostedId, recipientId, rewardId));
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShortProjectRewardView> getRewardsToBoostFromEcosystemNotLinkedToProject(UUID ecosystemId, UUID projectId) {
        return shortRewardViewRepository.findRewardsToBoosWithNodeGuardiansForEcosystemIdNotLinkedToProject(ecosystemId, projectId).stream()
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
