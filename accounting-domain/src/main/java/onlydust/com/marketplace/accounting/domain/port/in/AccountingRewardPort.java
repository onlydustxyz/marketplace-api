package onlydust.com.marketplace.accounting.domain.port.in;

import onlydust.com.marketplace.accounting.domain.model.BatchPayment;
import onlydust.com.marketplace.accounting.domain.model.Invoice;
import onlydust.com.marketplace.accounting.domain.view.BackofficeRewardView;
import onlydust.com.marketplace.accounting.domain.view.BatchPaymentDetailsView;
import onlydust.com.marketplace.kernel.model.RewardStatus;
import onlydust.com.marketplace.kernel.pagination.Page;

import java.util.Date;
import java.util.List;
import java.util.Set;

public interface AccountingRewardPort {
    List<BackofficeRewardView> searchForBatchPaymentByInvoiceIds(List<Invoice.Id> invoiceIds);

    List<BackofficeRewardView> findByInvoiceId(Invoice.Id invoiceId);

    List<BatchPaymentDetailsView> createBatchPaymentsForInvoices(List<Invoice.Id> invoiceIds);

    void markBatchPaymentAsPaid(BatchPayment.Id batchPaymentId, String transactionHash);

    Page<BatchPaymentDetailsView> findBatchPayments(int pageIndex, int pageSize, Set<BatchPayment.Status> statuses);

    BatchPaymentDetailsView findBatchPaymentById(BatchPayment.Id batchPaymentId);

    Page<BackofficeRewardView> getRewards(int pageIndex, int pageSize,
                                          List<RewardStatus> statuses,
                                          Date fromRequestedAt, Date toRequestedAt,
                                          Date fromProcessedAt, Date toProcessedAt);

    String exportRewardsCSV(List<RewardStatus> statuses,
                            Date fromRequestedAt, Date toRequestedAt,
                            Date fromProcessedAt, Date toProcessedAt);

    void notifyAllNewPaidRewards();
}
