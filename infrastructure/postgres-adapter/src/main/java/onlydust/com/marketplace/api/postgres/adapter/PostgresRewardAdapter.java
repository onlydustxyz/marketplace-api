package onlydust.com.marketplace.api.postgres.adapter;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.model.Payment;
import onlydust.com.marketplace.accounting.domain.model.ProjectId;
import onlydust.com.marketplace.accounting.domain.model.RewardId;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.BillingProfile;
import onlydust.com.marketplace.accounting.domain.model.user.GithubUserId;
import onlydust.com.marketplace.accounting.domain.port.out.AccountingRewardStoragePort;
import onlydust.com.marketplace.accounting.domain.view.*;
import onlydust.com.marketplace.api.postgres.adapter.entity.backoffice.read.BackofficeRewardViewEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.backoffice.read.BoEarningsViewEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.PaymentShortViewEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.ShortRewardViewEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.BatchPaymentEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.BatchPaymentRewardEntity;
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
import java.util.stream.Collectors;

@AllArgsConstructor
public class PostgresRewardAdapter implements RewardStoragePort, AccountingRewardStoragePort, BoostedRewardStoragePort {
    private final ShortProjectViewEntityRepository shortProjectViewEntityRepository;
    private final BatchPaymentRepository batchPaymentRepository;
    private final RewardDetailsViewRepository rewardDetailsViewRepository;
    private final BackofficeRewardViewRepository backofficeRewardViewRepository;
    private final RewardRepository rewardRepository;
    private final PaymentShortViewRepository paymentShortViewRepository;
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
    @Transactional(readOnly = true)
    public Page<BatchPaymentShortView> findPayments(int pageIndex, int pageSize, Set<Payment.Status> statuses) {
        if (statuses == null || statuses.isEmpty()) {
            statuses = EnumSet.allOf(Payment.Status.class);
        }
        final var page = paymentShortViewRepository.findByStatuses(statuses.stream().map(Enum::name).collect(Collectors.toSet()),
                PageRequest.of(pageIndex, pageSize, Sort.by("created_at").descending()));
        return Page.<BatchPaymentShortView>builder()
                .content(page.getContent().stream().map(PaymentShortViewEntity::toDomain).toList())
                .totalPageNumber(page.getTotalPages())
                .totalItemNumber((int) page.getTotalElements())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<BatchPaymentShortView> findPaymentsByIds(Set<Payment.Id> paymentIds) {
        return paymentShortViewRepository.findByIds(paymentIds.stream().map(UuidWrapper::value).collect(Collectors.toSet()))
                .stream().map(PaymentShortViewEntity::toDomain).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<BatchPaymentDetailsView> findPaymentDetailsById(Payment.Id batchPaymentId) {
        return batchPaymentRepository.findById(batchPaymentId.value()).map(this::getBatchPaymentDetailsView);
    }

    private BatchPaymentDetailsView getBatchPaymentDetailsView(BatchPaymentEntity batchPayment) {
        return BatchPaymentDetailsView.builder()
                .payment(batchPayment.toDomain())
                .rewardViews(
                        backofficeRewardViewRepository.findAllByRewardIds(batchPayment.getRewards().stream().map(BatchPaymentRewardEntity::rewardId).toList()).stream()
                                .map(BackofficeRewardViewEntity::toDomain)
                                .toList())
                .build();
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
                .content(page.getContent().stream().map(BackofficeRewardViewEntity::toDomain).toList())
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
                ).stream().map(BoEarningsViewEntity::toDomain).toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<RewardDetailsView> findPaidRewardsToNotify() {
        return backofficeRewardViewRepository.findPaidRewardsToNotify().stream().map(BackofficeRewardViewEntity::toDomain).toList();
    }

    @Override
    @Transactional
    public void markRewardsAsPaymentNotified(List<RewardId> rewardIds) {
        rewardDetailsViewRepository.markRewardAsPaymentNotified(rewardIds.stream().map(UuidWrapper::value).toList());
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
        return backofficeRewardViewRepository.findAllByRewardIds(List.of(id.value())).stream().findFirst().map(BackofficeRewardViewEntity::toDomain);
    }

    @Override
    public Optional<ShortRewardDetailsView> getShortReward(RewardId rewardId) {
        return shortRewardViewRepository.findById(rewardId.value()).map(ShortRewardViewEntity::toAccountingDomain);
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
                .map(ShortRewardViewEntity::toProjectDomain).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Integer> getBoostedRewardsCountByRecipientId(Long recipientId) {
        return shortRewardViewRepository.countNumberOfBoostByRecipientId(recipientId);
    }
}
