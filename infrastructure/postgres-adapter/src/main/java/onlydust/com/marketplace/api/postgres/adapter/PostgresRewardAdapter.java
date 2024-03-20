package onlydust.com.marketplace.api.postgres.adapter;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.model.Currency;
import onlydust.com.marketplace.accounting.domain.model.Invoice;
import onlydust.com.marketplace.accounting.domain.model.Payment;
import onlydust.com.marketplace.accounting.domain.model.RewardId;
import onlydust.com.marketplace.accounting.domain.port.out.AccountingRewardStoragePort;
import onlydust.com.marketplace.accounting.domain.port.out.CurrencyStorage;
import onlydust.com.marketplace.accounting.domain.view.BackofficeRewardView;
import onlydust.com.marketplace.accounting.domain.view.BatchPaymentDetailsView;
import onlydust.com.marketplace.api.postgres.adapter.entity.backoffice.read.BackofficeRewardViewEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.BatchPaymentEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.BatchPaymentRewardEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.RewardEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.RewardStatusEntity;
import onlydust.com.marketplace.api.postgres.adapter.mapper.ProjectMapper;
import onlydust.com.marketplace.api.postgres.adapter.repository.RewardDetailsViewRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.RewardRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.RewardViewRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.ShortProjectViewEntityRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.backoffice.BatchPaymentRepository;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
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
    private final RewardViewRepository rewardViewRepository;
    private final RewardDetailsViewRepository rewardDetailsViewRepository;
    private final RewardRepository rewardRepository;
    private final CurrencyStorage currencyStorage;

    @Override
    public void save(Reward reward) {
        final var currency = currencyStorage.get(Currency.Id.of(reward.currencyId().value()))
                .orElseThrow(() -> OnlyDustException.internalServerError("Currency %s not found".formatted(reward.currencyId())));
        rewardRepository.save(RewardEntity.of(reward, currency));
    }

    @Override
    public void delete(UUID rewardId) {
        rewardRepository.deleteById(rewardId);
    }

    @Override
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
    public List<BackofficeRewardView> searchRewards(List<Invoice.Status> statuses, List<Invoice.Id> invoiceIds) {
        return rewardDetailsViewRepository.findAllByInvoiceStatusesAndInvoiceIds(
                        statuses != null ? statuses.stream().map(Invoice.Status::toString).toList() : null,
                        invoiceIds != null ? invoiceIds.stream().map(Invoice.Id::value).toList() : null
                )
                .stream()
                .map(BackofficeRewardViewEntity::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<BackofficeRewardView> getInvoiceRewards(@NonNull Invoice.Id invoiceId) {
        return rewardDetailsViewRepository.findAllByInvoiceId(invoiceId.value())
                .stream()
                .map(BackofficeRewardViewEntity::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Payment> findBatchPayment(Payment.Id batchPaymentId) {
        return batchPaymentRepository.findById(batchPaymentId.value()).map(BatchPaymentEntity::toDomain);
    }

    @Override
    @Transactional
    public void saveBatchPayment(Payment payment) {
        batchPaymentRepository.save(BatchPaymentEntity.fromDomain(payment));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BatchPaymentDetailsView> findBatchPaymentDetails(int pageIndex, int pageSize, Set<Payment.Status> statuses) {
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
    public Optional<BatchPaymentDetailsView> findBatchPaymentDetailsById(Payment.Id batchPaymentId) {
        return batchPaymentRepository.findById(batchPaymentId.value()).map(this::getBatchPaymentDetailsView);
    }

    private BatchPaymentDetailsView getBatchPaymentDetailsView(BatchPaymentEntity batchPayment) {
        return BatchPaymentDetailsView.builder()
                .payment(batchPayment.toDomain())
                .rewardViews(
                        rewardDetailsViewRepository.findAllByRewardIds(batchPayment.getRewards().stream().map(BatchPaymentRewardEntity::rewardId).toList()).stream()
                                .map(BackofficeRewardViewEntity::toDomain)
                                .toList())
                .build();
    }

    @Override
    public List<BackofficeRewardView> findRewardsById(Set<RewardId> rewardIds) {
        return rewardDetailsViewRepository.findAllByRewardIds(rewardIds.stream().map(UuidWrapper::value).toList())
                .stream()
                .map(BackofficeRewardViewEntity::toDomain)
                .toList();
    }

    @Override
    public Page<BackofficeRewardView> findRewards(int pageIndex, int pageSize,
                                                  @NonNull Set<RewardStatus> statuses,
                                                  Date fromRequestedAt, Date toRequestedAt,
                                                  Date fromProcessedAt, Date toProcessedAt) {
        final var page = rewardDetailsViewRepository.findAllByStatusesAndDates(
                statuses.stream().map(RewardStatusEntity::from).map(RewardStatusEntity.Status::toString).toList(),
                fromRequestedAt, toRequestedAt,
                fromProcessedAt, toProcessedAt,
                PageRequest.of(pageIndex, pageSize, Sort.by("requested_at").descending())
        );

        return Page.<BackofficeRewardView>builder()
                .content(page.getContent().stream().map(BackofficeRewardViewEntity::toDomain).toList())
                .totalItemNumber((int) page.getTotalElements())
                .totalPageNumber(page.getTotalPages())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<BackofficeRewardView> findPaidRewardsToNotify() {
        return rewardDetailsViewRepository.findPaidRewardsToNotify().stream().map(BackofficeRewardViewEntity::toDomain).toList();
    }

    @Override
    @Transactional
    public void markRewardsAsPaymentNotified(List<RewardId> rewardIds) {
        rewardViewRepository.markRewardAsPaymentNotified(rewardIds.stream().map(UuidWrapper::value).toList());
    }

    @Override
    @Transactional
    public void saveAll(List<Payment> payments) {
        batchPaymentRepository.saveAll(payments.stream().map(BatchPaymentEntity::fromDomain).toList());
    }
}
