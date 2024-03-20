package onlydust.com.marketplace.accounting.domain.port.in;

import onlydust.com.marketplace.accounting.domain.model.Payment;
import onlydust.com.marketplace.accounting.domain.model.Invoice;
import onlydust.com.marketplace.accounting.domain.view.BatchPaymentDetailsView;
import onlydust.com.marketplace.kernel.pagination.Page;

import java.util.List;
import java.util.Set;

public interface BatchPaymentPort {

    List<BatchPaymentDetailsView> createBatchPaymentsForInvoices(List<Invoice.Id> invoiceIds);

    void markBatchPaymentAsPaid(Payment.Id batchPaymentId, String transactionHash);

    Page<BatchPaymentDetailsView> findBatchPayments(int pageIndex, int pageSize, Set<Payment.Status> statuses);

    BatchPaymentDetailsView findBatchPaymentById(Payment.Id batchPaymentId);

}
