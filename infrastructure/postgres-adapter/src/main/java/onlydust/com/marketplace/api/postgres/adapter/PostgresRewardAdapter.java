package onlydust.com.marketplace.api.postgres.adapter;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.model.BatchPayment;
import onlydust.com.marketplace.accounting.domain.model.Invoice;
import onlydust.com.marketplace.accounting.domain.port.out.AccountingRewardStoragePort;
import onlydust.com.marketplace.accounting.domain.view.BatchPaymentDetailsView;
import onlydust.com.marketplace.accounting.domain.view.PayableRewardWithPayoutInfoView;
import onlydust.com.marketplace.accounting.domain.view.RewardDetailsView;
import onlydust.com.marketplace.accounting.domain.view.RewardView;
import onlydust.com.marketplace.api.postgres.adapter.entity.backoffice.read.InvoiceRewardViewEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.BatchPaymentDetailsViewEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.PayableRewardWithPayoutInfoViewEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.BatchPaymentEntity;
import onlydust.com.marketplace.api.postgres.adapter.mapper.ProjectMapper;
import onlydust.com.marketplace.api.postgres.adapter.repository.*;
import onlydust.com.marketplace.api.postgres.adapter.repository.backoffice.BatchPaymentRepository;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import onlydust.com.marketplace.kernel.model.UuidWrapper;
import onlydust.com.marketplace.kernel.pagination.Page;
import onlydust.com.marketplace.kernel.pagination.PaginationHelper;
import onlydust.com.marketplace.project.domain.model.Project;
import onlydust.com.marketplace.project.domain.model.Reward;
import onlydust.com.marketplace.project.domain.port.output.RewardStoragePort;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@AllArgsConstructor
public class PostgresRewardAdapter implements RewardStoragePort, AccountingRewardStoragePort {

    private final ShortProjectViewEntityRepository shortProjectViewEntityRepository;
    private final InvoiceRewardViewRepository invoiceRewardViewRepository;
    private final PayableRewardWithPayoutInfoViewRepository payableRewardWithPayoutInfoViewRepository;
    private final BatchPaymentRepository batchPaymentRepository;
    private final BatchPaymentDetailsViewRepository batchPaymentDetailsViewRepository;
    private final RewardViewRepository rewardViewRepository;

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
    public List<RewardView> searchRewards(List<Invoice.Status> statuses, List<Invoice.Id> invoiceIds) {
        return invoiceRewardViewRepository.findAllByInvoiceStatusesAndInvoiceIds(
                        statuses != null ? statuses.stream().map(Invoice.Status::toString).toList() : null,
                        invoiceIds != null ? invoiceIds.stream().map(Invoice.Id::value).toList() : null
                )
                .stream()
                .map(InvoiceRewardViewEntity::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<RewardView> getInvoiceRewards(@NonNull Invoice.Id invoiceId) {
        return invoiceRewardViewRepository.findAllByInvoiceId(invoiceId.value())
                .stream()
                .map(InvoiceRewardViewEntity::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PayableRewardWithPayoutInfoView> findPayableRewardsWithPayoutInfoForInvoices(List<Invoice.Id> invoiceIds) {
        return payableRewardWithPayoutInfoViewRepository.findAllByInvoiceIds(invoiceIds.stream().map(UuidWrapper::value).toList())
                .stream().map(PayableRewardWithPayoutInfoViewEntity::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PayableRewardWithPayoutInfoView> findPayableRewardsWithPayoutInfoForBatchPayment(BatchPayment.Id batchPaymentId) {
        return payableRewardWithPayoutInfoViewRepository.findAllByBatchPaymentId(batchPaymentId.value())
                .stream()
                .map(PayableRewardWithPayoutInfoViewEntity::toDomain)
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
        final int count = batchPaymentDetailsViewRepository.countAll().intValue();
        return Page.<BatchPayment>builder()
                .content(batchPaymentDetailsViewRepository.findAllBy(pageIndex, pageSize).stream().map(BatchPaymentDetailsViewEntity::toDomain).toList())
                .totalPageNumber(PaginationHelper.calculateTotalNumberOfPage(pageSize, count))
                .totalItemNumber(count)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<BatchPaymentDetailsView> findBatchPaymentDetailsById(BatchPayment.Id batchPaymentId) {
        return batchPaymentDetailsViewRepository.findById(batchPaymentId.value())
                .map(batchPaymentDetailsView ->
                        BatchPaymentDetailsView.builder()
                                .batchPayment(batchPaymentDetailsView.toDomain())
                                .rewardViews(invoiceRewardViewRepository.findAllByRewardIds(batchPaymentDetailsView.getRewardIds()).stream()
                                        .map(InvoiceRewardViewEntity::toDomain)
                                        .toList())
                                .build()
                );
    }

    @Override
    public Page<RewardDetailsView> findRewards(int pageIndex, int pageSize,
                                               @NonNull Set<RewardDetailsView.Status> statuses,
                                               Date fromRequestedAt, Date toRequestedAt,
                                               Date fromProcessedAt, Date toProcessedAt) {
        //TODO
//        final var page = rewardViewRepository.findAllByStatusesAndDates(
//                statuses.stream().map(RewardDetailsView.Status::toString).toList(),
//                fromRequestedAt, toRequestedAt,
//                fromProcessedAt, toProcessedAt,
//                PageRequest.of(pageIndex, pageSize, Sort.by("requested_at").descending())
//        );
//
//        return Page.<RewardDetailsView>builder()
//                .content(page.getContent().stream().map(RewardDetailsViewEntity::toDomain).toList())
//                .totalItemNumber((int) page.getTotalElements())
//                .totalPageNumber(page.getTotalPages())
//                .build();
        return null;
    }
}
