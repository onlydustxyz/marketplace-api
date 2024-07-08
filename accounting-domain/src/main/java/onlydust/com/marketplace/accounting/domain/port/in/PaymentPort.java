package onlydust.com.marketplace.accounting.domain.port.in;

import onlydust.com.marketplace.accounting.domain.model.Invoice;
import onlydust.com.marketplace.accounting.domain.model.Payment;
import onlydust.com.marketplace.accounting.domain.view.BatchPaymentShortView;
import onlydust.com.marketplace.kernel.pagination.Page;

import java.util.List;
import java.util.Set;

public interface PaymentPort {

    List<Payment> createPaymentsForInvoices(List<Invoice.Id> invoiceIds);

    void markPaymentAsPaid(Payment.Id batchPaymentId, String transactionHash);

    // TODO: move to read-api
    Page<BatchPaymentShortView> findPayments(int pageIndex, int pageSize, Set<Payment.Status> statuses);

    // TODO: move to read-api
    List<BatchPaymentShortView> findPaymentsByIds(Set<Payment.Id> ids);

    void deletePaymentById(Payment.Id paymentId);
}
