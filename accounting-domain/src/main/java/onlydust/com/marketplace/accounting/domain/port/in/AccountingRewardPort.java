package onlydust.com.marketplace.accounting.domain.port.in;

import onlydust.com.marketplace.accounting.domain.model.BatchPayment;
import onlydust.com.marketplace.accounting.domain.model.Invoice;
import onlydust.com.marketplace.accounting.domain.view.RewardView;

import java.util.List;

public interface AccountingRewardPort {
    List<RewardView> searchForApprovedInvoiceIds(List<Invoice.Id> invoiceIds);

    List<RewardView> findByInvoiceId(Invoice.Id invoiceId);

    List<BatchPayment> createBatchPaymentsForInvoices(List<Invoice.Id> invoiceIds);

    void markBatchPaymentAsPaid(BatchPayment.Id batchPaymentId, String transactionHash);
}
