package onlydust.com.marketplace.api.postgres.adapter;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.model.BatchPayment;
import onlydust.com.marketplace.accounting.domain.model.Invoice;
import onlydust.com.marketplace.accounting.domain.model.RewardId;
import onlydust.com.marketplace.accounting.domain.port.out.AccountingRewardStoragePort;
import onlydust.com.marketplace.accounting.domain.view.BackofficeRewardView;
import onlydust.com.marketplace.accounting.domain.view.BatchPaymentDetailsView;
import onlydust.com.marketplace.accounting.domain.view.RewardWithPayoutInfoView;
import onlydust.com.marketplace.api.postgres.adapter.entity.backoffice.read.BackofficeRewardViewEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.backoffice.read.BoRewardWithPayoutInfoEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.BatchPaymentEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.RewardStatusEntity;
import onlydust.com.marketplace.api.postgres.adapter.mapper.ProjectMapper;
import onlydust.com.marketplace.api.postgres.adapter.repository.PayableRewardWithPayoutInfoViewRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.RewardDetailsViewRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.RewardViewRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.ShortProjectViewEntityRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.backoffice.BatchPaymentRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.backoffice.BoRewardWithPayoutInfoRepository;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import onlydust.com.marketplace.kernel.model.RewardStatus;
import onlydust.com.marketplace.kernel.model.UuidWrapper;
import onlydust.com.marketplace.kernel.pagination.Page;
import onlydust.com.marketplace.kernel.pagination.PaginationHelper;
import onlydust.com.marketplace.project.domain.model.Project;
import onlydust.com.marketplace.project.domain.model.Reward;
import onlydust.com.marketplace.project.domain.port.output.RewardStoragePort;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@AllArgsConstructor
public class PostgresRewardAdapter implements RewardStoragePort, AccountingRewardStoragePort {

    private final ShortProjectViewEntityRepository shortProjectViewEntityRepository;
    private final PayableRewardWithPayoutInfoViewRepository payableRewardWithPayoutInfoViewRepository;
    private final BatchPaymentRepository batchPaymentRepository;
    private final RewardViewRepository rewardViewRepository;
    private final RewardDetailsViewRepository rewardDetailsViewRepository;
    private final BoRewardWithPayoutInfoRepository boRewardWithPayoutInfoRepository;

    @Override
    public void save(Reward reward) {
        throw OnlyDustException.internalServerError("Not implemented for v1");
    }

    @Override
    public void delete(UUID rewardId) {
        throw OnlyDustException.internalServerError("Not implemented for v1");
    }

    @Override
    public Optional<Reward> get(UUID rewardId) {
        throw OnlyDustException.internalServerError("Not implemented for v1");
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
    public Optional<BatchPayment> findBatchPayment(BatchPayment.Id batchPaymentId) {
        return batchPaymentRepository.findById(batchPaymentId.value()).map(BatchPaymentEntity::toDomain);
    }

    @Override
    @Transactional
    public void saveBatchPayment(BatchPayment batchPayment) {
        batchPaymentRepository.save(BatchPaymentEntity.fromDomain(batchPayment));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BatchPayment> findBatchPayments(int pageIndex, int pageSize) {
        final int count = batchPaymentRepository.countAllByStatus(BatchPaymentEntity.Status.PAID);
        return Page.<BatchPayment>builder()
                .content(batchPaymentRepository.findAllByStatus(BatchPaymentEntity.Status.PAID,
                                PageRequest.of(pageIndex, pageSize, Sort.by("createdAt").descending()))
                        .stream().map(BatchPaymentEntity::toDomain).toList())
                .totalPageNumber(PaginationHelper.calculateTotalNumberOfPage(pageSize, count))
                .totalItemNumber(count)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<BatchPaymentDetailsView> findBatchPaymentDetailsById(BatchPayment.Id batchPaymentId) {
        return batchPaymentRepository.findById(batchPaymentId.value())
                .map(batchPayment ->
                        BatchPaymentDetailsView.builder()
                                .batchPayment(batchPayment.toDomain())
                                .rewardViews(rewardDetailsViewRepository.findAllByRewardIds(batchPayment.getRewardIds()).stream()
                                        .map(BackofficeRewardViewEntity::toDomain)
                                        .toList())
                                .build()
                );
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
    @Transactional(readOnly = true)
    public List<RewardWithPayoutInfoView> getRewardWithPayoutInfoOfInvoices(List<Invoice.Id> invoiceIds) {
        return boRewardWithPayoutInfoRepository.findByInvoiceIds(invoiceIds.stream().map(UuidWrapper::value).toList())
                .stream()
                .map(BoRewardWithPayoutInfoEntity::toDomain)
                .toList();
    }
}
