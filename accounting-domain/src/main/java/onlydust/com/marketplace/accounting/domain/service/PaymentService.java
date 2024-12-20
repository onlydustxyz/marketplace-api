package onlydust.com.marketplace.accounting.domain.service;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.model.Invoice;
import onlydust.com.marketplace.accounting.domain.model.Network;
import onlydust.com.marketplace.accounting.domain.model.Payment;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.Wallet;
import onlydust.com.marketplace.accounting.domain.port.in.AccountingFacadePort;
import onlydust.com.marketplace.accounting.domain.port.in.BlockchainFacadePort;
import onlydust.com.marketplace.accounting.domain.port.in.PaymentPort;
import onlydust.com.marketplace.accounting.domain.port.out.AccountingRewardStoragePort;
import onlydust.com.marketplace.accounting.domain.port.out.InvoiceStoragePort;
import onlydust.com.marketplace.kernel.model.RewardId;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toMap;
import static onlydust.com.marketplace.kernel.exception.OnlyDustException.badRequest;
import static onlydust.com.marketplace.kernel.exception.OnlyDustException.notFound;

@AllArgsConstructor
public class PaymentService implements PaymentPort {
    private final AccountingRewardStoragePort accountingRewardStoragePort;
    private final InvoiceStoragePort invoiceStoragePort;
    private final AccountingFacadePort accountingFacadePort;
    private final BlockchainFacadePort blockchainFacadePort;

    @NonNull
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

        final var blockchain = payment.network().blockchain().orElseThrow();
        final var transaction = blockchainFacadePort.getTransaction(blockchain, transactionReference)
                .orElseThrow(() -> notFound("Transaction %s not found on blockchain %s".formatted(transactionReference, blockchain.pretty())));

        final var updatedPayment = payment.toBuilder()
                .status(Payment.Status.PAID)
                .transactionHash(transaction.reference())
                .confirmedAt(transaction.timestamp())
                .build();

        accountingFacadePort.confirm(updatedPayment);
        accountingRewardStoragePort.savePayment(updatedPayment);
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
            payment.csv(paymentExporter(payment.network()).csv(rewards, wallets, rewardInvoices));
        });

        accountingRewardStoragePort.saveAll(payments);
        return payments;
    }

    private PaymentExporter paymentExporter(Network network) {
        return switch (network) {
            case NEAR -> new NearPaymentExporter();
            default -> new DefaultPaymentExporter();
        };
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
