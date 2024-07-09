package onlydust.com.marketplace.accounting.domain.port.in;

import onlydust.com.marketplace.accounting.domain.model.Invoice;
import onlydust.com.marketplace.accounting.domain.model.Payment;

import java.util.List;

public interface PaymentPort {

    List<Payment> createPaymentsForInvoices(List<Invoice.Id> invoiceIds);

    void markPaymentAsPaid(Payment.Id batchPaymentId, String transactionHash);

    void deletePaymentById(Payment.Id paymentId);
}
