package onlydust.com.marketplace.api.postgres.adapter;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.model.Payment;
import onlydust.com.marketplace.accounting.domain.model.RewardId;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.BillingProfile;
import onlydust.com.marketplace.accounting.domain.port.out.AccountingRewardStoragePort;
import onlydust.com.marketplace.accounting.domain.view.BatchPaymentDetailsView;
import onlydust.com.marketplace.accounting.domain.view.RewardDetailsView;
import onlydust.com.marketplace.api.postgres.adapter.entity.backoffice.read.BackofficeRewardViewEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.BatchPaymentEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.BatchPaymentRewardEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.RewardEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.RewardStatusEntity;
import onlydust.com.marketplace.api.postgres.adapter.mapper.ProjectMapper;
import onlydust.com.marketplace.api.postgres.adapter.repository.BackofficeRewardViewRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.RewardDetailsViewRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.RewardRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.ShortProjectViewEntityRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.backoffice.BatchPaymentRepository;
import onlydust.com.marketplace.kernel.model.RewardStatus;
import onlydust.com.marketplace.kernel.model.UuidWrapper;
import onlydust.com.marketplace.kernel.pagination.Page;
import onlydust.com.marketplace.project.domain.model.Project;
import onlydust.com.marketplace.project.domain.model.Reward;
import onlydust.com.marketplace.project.domain.port.output.RewardStoragePort;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@AllArgsConstructor
public class PostgresRewardAdapter implements RewardStoragePort, AccountingRewardStoragePort {
    private final ShortProjectViewEntityRepository shortProjectViewEntityRepository;
    private final BatchPaymentRepository batchPaymentRepository;
    private final RewardDetailsViewRepository rewardDetailsViewRepository;
    private final BackofficeRewardViewRepository backofficeRewardViewRepository;
    private final RewardRepository rewardRepository;

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
    public Page<BatchPaymentDetailsView> findPaymentDetails(int pageIndex, int pageSize, Set<Payment.Status> statuses) {
        if (statuses == null || statuses.isEmpty()) {
            statuses = EnumSet.allOf(Payment.Status.class);
        }
        final var page = batchPaymentRepository.findAllByStatusIsIn(statuses.stream().map(BatchPaymentEntity.Status::of).collect(Collectors.toSet()),
                PageRequest.of(pageIndex, pageSize, Sort.by("createdAt").descending()));
        return Page.<BatchPaymentDetailsView>builder()
                .content(page.getContent().stream().map(this::getBatchPaymentDetailsView).toList())
                .totalPageNumber(page.getTotalPages())
                .totalItemNumber((int) page.getTotalElements())
                .build();
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
    public List<RewardDetailsView> findRewardsById(Set<RewardId> rewardIds) {
        return backofficeRewardViewRepository.findAllByRewardIds(rewardIds.stream().map(UuidWrapper::value).toList())
                .stream()
                .map(BackofficeRewardViewEntity::toDomain)
                .toList();
    }

    @Override
    @Transactional
    public Page<RewardDetailsView> findRewards(int pageIndex, int pageSize,
                                               @NonNull Set<RewardStatus.Input> statuses,
                                               @NonNull List<BillingProfile.Id> billingProfileIds,
                                               Date fromRequestedAt, Date toRequestedAt,
                                               Date fromProcessedAt, Date toProcessedAt) {
        final var page = backofficeRewardViewRepository.findAllByStatusesAndDates(
                statuses.stream().map(rewardStatus -> RewardStatusEntity.from(rewardStatus)).map(RewardStatusEntity.Status::toString).toList(),
                billingProfileIds.stream().map(BillingProfile.Id::value).toList(),
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
    @Transactional
    public Optional<RewardDetailsView> getReward(RewardId id) {
        return backofficeRewardViewRepository.findAllByRewardIds(List.of(id.value())).stream().findFirst().map(BackofficeRewardViewEntity::toDomain);
    }
}
