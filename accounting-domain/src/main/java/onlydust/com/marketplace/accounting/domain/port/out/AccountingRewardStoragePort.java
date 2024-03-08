package onlydust.com.marketplace.accounting.domain.port.out;

import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.model.BatchPayment;
import onlydust.com.marketplace.accounting.domain.model.Invoice;
import onlydust.com.marketplace.accounting.domain.model.RewardId;
import onlydust.com.marketplace.accounting.domain.view.BatchPaymentDetailsView;
import onlydust.com.marketplace.accounting.domain.view.PayableRewardWithPayoutInfoView;
import onlydust.com.marketplace.accounting.domain.view.RewardDetailsView;
import onlydust.com.marketplace.kernel.pagination.Page;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface AccountingRewardStoragePort {

    List<RewardDetailsView> searchRewards(List<Invoice.Status> statuses, List<Invoice.Id> invoiceIds);

    List<RewardDetailsView> getInvoiceRewards(@NonNull Invoice.Id invoiceId);

    List<PayableRewardWithPayoutInfoView> findPayableRewardsWithPayoutInfoForInvoices(List<Invoice.Id> invoiceIds);

    List<PayableRewardWithPayoutInfoView> findPayableRewardsWithPayoutInfoForBatchPayment(BatchPayment.Id batchPaymentId);

    Optional<BatchPayment> findBatchPayment(BatchPayment.Id batchPaymentId);

    void saveBatchPayment(BatchPayment updatedBatchPayment);

    Page<BatchPayment> findBatchPayments(int pageIndex, int pageSize);

    Optional<BatchPaymentDetailsView> findBatchPaymentDetailsById(BatchPayment.Id batchPaymentId);

    Page<RewardDetailsView> findRewards(int pageIndex, int pageSize,
                                        @NonNull Set<RewardDetailsView.Status> statuses,
                                        Date fromRequestedAt, Date toRequestedAt,
                                        Date fromProcessedAt, Date toProcessedAt);

    List<RewardDetailsView> findPaidRewardsToNotify();

    void markRewardsAsPaymentNotified(List<RewardId> rewardId);
}
