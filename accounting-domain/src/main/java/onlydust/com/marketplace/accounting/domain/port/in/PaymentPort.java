package onlydust.com.marketplace.accounting.domain.port.in;

import onlydust.com.marketplace.accounting.domain.model.Invoice;
import onlydust.com.marketplace.accounting.domain.model.Payment;
import onlydust.com.marketplace.accounting.domain.view.BatchPaymentShortView;

import java.util.List;
import java.util.Set;

public interface PaymentPort {

    List<Payment> createPaymentsForInvoices(List<Invoice.Id> invoiceIds);

    void markPaymentAsPaid(Payment.Id batchPaymentId, String transactionHash);

    // TODO: move to read-api
    List<BatchPaymentShortView> findPaymentsByIds(Set<Payment.Id> ids);

    void deletePaymentById(Payment.Id paymentId);
}
