package onlydust.com.marketplace.accounting.domain.port.in;

import onlydust.com.marketplace.accounting.domain.model.BatchPayment;
import onlydust.com.marketplace.accounting.domain.model.Invoice;
import onlydust.com.marketplace.accounting.domain.view.BackofficeRewardView;
import onlydust.com.marketplace.accounting.domain.view.BatchPaymentDetailsView;
import onlydust.com.marketplace.kernel.pagination.Page;

import java.util.Date;
import java.util.List;

public interface AccountingRewardPort {
    List<BackofficeRewardView> searchForBatchPaymentByInvoiceIds(List<Invoice.Id> invoiceIds);

    List<BackofficeRewardView> findByInvoiceId(Invoice.Id invoiceId);

    List<BatchPayment> createBatchPaymentsForInvoices(List<Invoice.Id> invoiceIds);

    void markBatchPaymentAsPaid(BatchPayment.Id batchPaymentId, String transactionHash);

    Page<BatchPayment> findBatchPayments(int pageIndex, int pageSize);

    BatchPaymentDetailsView findBatchPaymentById(BatchPayment.Id batchPaymentId);

    Page<BackofficeRewardView> getRewards(int pageIndex, int pageSize,
                                          List<BackofficeRewardView.Status> statuses,
                                          Date fromRequestedAt, Date toRequestedAt,
                                          Date fromProcessedAt, Date toProcessedAt);

    String exportRewardsCSV(List<BackofficeRewardView.Status> statuses,
                            Date fromRequestedAt, Date toRequestedAt,
                            Date fromProcessedAt, Date toProcessedAt);

    void notifyAllNewPaidRewards();
}
