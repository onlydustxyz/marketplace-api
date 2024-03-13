package onlydust.com.marketplace.accounting.domain.port.out;

import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.model.BatchPayment;
import onlydust.com.marketplace.accounting.domain.model.Invoice;
import onlydust.com.marketplace.accounting.domain.model.RewardId;
import onlydust.com.marketplace.accounting.domain.view.BackofficeRewardView;
import onlydust.com.marketplace.accounting.domain.view.BatchPaymentDetailsView;
import onlydust.com.marketplace.accounting.domain.view.RewardWithPayoutInfoView;
import onlydust.com.marketplace.kernel.model.RewardStatus;
import onlydust.com.marketplace.kernel.pagination.Page;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface AccountingRewardStoragePort {

    List<BackofficeRewardView> searchRewards(List<Invoice.Status> statuses, List<Invoice.Id> invoiceIds);

    List<BackofficeRewardView> getInvoiceRewards(@NonNull Invoice.Id invoiceId);

    Optional<BatchPayment> findBatchPayment(BatchPayment.Id batchPaymentId);

    void saveBatchPayment(BatchPayment updatedBatchPayment);

    Page<BatchPayment> findBatchPayments(int pageIndex, int pageSize);

    Optional<BatchPaymentDetailsView> findBatchPaymentDetailsById(BatchPayment.Id batchPaymentId);

    Page<BackofficeRewardView> findRewards(int pageIndex, int pageSize,
                                           @NonNull Set<RewardStatus> statuses,
                                           Date fromRequestedAt, Date toRequestedAt,
                                           Date fromProcessedAt, Date toProcessedAt);

    List<BackofficeRewardView> findPaidRewardsToNotify();

    void markRewardsAsPaymentNotified(List<RewardId> rewardId);

    List<RewardWithPayoutInfoView> getRewardWithPayoutInfoOfInvoices(List<Invoice.Id> invoiceIds);
}
