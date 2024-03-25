package onlydust.com.marketplace.accounting.domain.port.in;

import onlydust.com.marketplace.accounting.domain.model.Invoice;
import onlydust.com.marketplace.accounting.domain.model.Payment;
import onlydust.com.marketplace.accounting.domain.view.BatchPaymentDetailsView;
import onlydust.com.marketplace.kernel.pagination.Page;

import java.util.List;
import java.util.Set;

public interface PaymentPort {

    List<BatchPaymentDetailsView> createPaymentsForInvoices(List<Invoice.Id> invoiceIds);

    void markPaymentAsPaid(Payment.Id batchPaymentId, String transactionHash);

    Page<BatchPaymentDetailsView> findPayments(int pageIndex, int pageSize, Set<Payment.Status> statuses);

    BatchPaymentDetailsView findPaymentById(Payment.Id batchPaymentId);

    void deletePaymentById(Payment.Id paymentId);
}
