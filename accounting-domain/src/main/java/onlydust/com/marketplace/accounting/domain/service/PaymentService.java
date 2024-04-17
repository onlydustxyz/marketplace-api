package onlydust.com.marketplace.accounting.domain.service;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.accounting.domain.model.Invoice;
import onlydust.com.marketplace.accounting.domain.model.Network;
import onlydust.com.marketplace.accounting.domain.model.Payment;
import onlydust.com.marketplace.accounting.domain.model.RewardId;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.Wallet;
import onlydust.com.marketplace.accounting.domain.port.in.AccountingFacadePort;
import onlydust.com.marketplace.accounting.domain.port.in.PaymentPort;
import onlydust.com.marketplace.accounting.domain.port.out.AccountingRewardStoragePort;
import onlydust.com.marketplace.accounting.domain.port.out.InvoiceStoragePort;
import onlydust.com.marketplace.accounting.domain.view.BatchPaymentDetailsView;
import onlydust.com.marketplace.accounting.domain.view.BatchPaymentShortView;
import onlydust.com.marketplace.kernel.pagination.Page;
import org.jetbrains.annotations.NotNull;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.stream.Collectors.toMap;
import static onlydust.com.marketplace.kernel.exception.OnlyDustException.badRequest;
import static onlydust.com.marketplace.kernel.exception.OnlyDustException.notFound;

@AllArgsConstructor
public class PaymentService implements PaymentPort {
    private final AccountingRewardStoragePort accountingRewardStoragePort;
    private final InvoiceStoragePort invoiceStoragePort;
    private final AccountingFacadePort accountingFacadePort;

    @NotNull
    private static Map<RewardId, Wallet> walletsPerRewardForNetwork(Map<RewardId, Invoice> rewardInvoices, Network network) {
        return rewardInvoices.entrySet().stream()
                .map(e -> Map.entry(e.getKey(), e.getValue().billingProfileSnapshot().wallet(network)))
                .filter(e -> e.getValue().isPresent())
                .collect(toMap(Map.Entry::getKey, e -> e.getValue().get()));
    }

    @Override
    @Transactional
    public void markPaymentAsPaid(Payment.Id paymentId, String transactionReference) {
        final var payment = accountingRewardStoragePort.findPayment(paymentId)
                .orElseThrow(() -> notFound("Batch payment %s not found".formatted(paymentId.value())));

        if (payment.status() != Payment.Status.TO_PAY) {
            throw badRequest("Batch payment %s is already paid".formatted(paymentId.value()));
        }

        payment.network().validateTransactionReference(transactionReference);

        final Payment updatedPayment = payment.toBuilder()
                .status(Payment.Status.PAID)
                .transactionHash(transactionReference)
                .confirmedAt(ZonedDateTime.now()) // TODO use the transaction timestamp
                .build();

        accountingFacadePort.confirm(updatedPayment);
        accountingRewardStoragePort.savePayment(updatedPayment);
    }

    @Override
    public Page<BatchPaymentShortView> findPayments(int pageIndex, int pageSize, Set<Payment.Status> statuses) {
        return accountingRewardStoragePort.findPayments(pageIndex, pageSize, statuses);
    }

    @Override
    public List<BatchPaymentShortView> findPaymentsByIds(Set<Payment.Id> ids) {
        return accountingRewardStoragePort.findPaymentsByIds(ids);
    }

    @Override
    public BatchPaymentDetailsView findPaymentById(Payment.Id paymentId) {
        return accountingRewardStoragePort.findPaymentDetailsById(paymentId)
                .orElseThrow(() -> notFound("Batch payment details %s not found".formatted(paymentId.value())));
    }

    @Override
    public List<Payment> createPaymentsForInvoices(List<Invoice.Id> invoiceIds) {

        final var invoices = invoiceStoragePort.getAll(invoiceIds);
        final var rewardInvoices = invoices.stream()
                .flatMap(invoice -> invoice.rewards().stream().map(reward -> Map.entry(reward.id(), invoice)))
                .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));

        final var payments = accountingFacadePort.pay(rewardInvoices.keySet());

        payments.forEach(payment -> {
            final Map<RewardId, Wallet> wallets = walletsPerRewardForNetwork(rewardInvoices, payment.network());
            final var rewards = payment.rewards().stream().sorted(Comparator.comparing(r -> r.id().value())).toList();
            payment.csv(PaymentExporter.csv(rewards, wallets));
        });

        accountingRewardStoragePort.saveAll(payments);
        return payments;
    }

    @Override
    @Transactional
    public void deletePaymentById(Payment.Id paymentId) {
        final var payment = accountingRewardStoragePort.findPayment(paymentId)
                .orElseThrow(() -> notFound("Batch payment %s not found".formatted(paymentId.value())));

        if (payment.status() != Payment.Status.TO_PAY) {
            throw badRequest("Batch payment %s is already paid".formatted(paymentId.value()));
        }

        accountingFacadePort.cancel(payment);
        accountingRewardStoragePort.deletePayment(paymentId);
    }
}
