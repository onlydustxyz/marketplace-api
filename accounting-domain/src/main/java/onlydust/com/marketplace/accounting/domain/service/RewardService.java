package onlydust.com.marketplace.accounting.domain.service;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.accounting.domain.model.Currency;
import onlydust.com.marketplace.accounting.domain.model.*;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.Wallet;
import onlydust.com.marketplace.accounting.domain.port.in.AccountingFacadePort;
import onlydust.com.marketplace.accounting.domain.port.in.AccountingRewardPort;
import onlydust.com.marketplace.accounting.domain.port.out.AccountingRewardStoragePort;
import onlydust.com.marketplace.accounting.domain.port.out.InvoiceStoragePort;
import onlydust.com.marketplace.accounting.domain.port.out.MailNotificationPort;
import onlydust.com.marketplace.accounting.domain.view.BackofficeRewardView;
import onlydust.com.marketplace.accounting.domain.view.BatchPaymentDetailsView;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import onlydust.com.marketplace.kernel.model.RewardStatus;
import onlydust.com.marketplace.kernel.pagination.Page;
import org.jetbrains.annotations.NotNull;

import javax.transaction.Transactional;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static java.util.stream.Collectors.groupingBy;
import static onlydust.com.marketplace.kernel.exception.OnlyDustException.badRequest;

@AllArgsConstructor
public class RewardService implements AccountingRewardPort {

    private static final List<Currency.Code> CURRENCY_CODES_AVAILABLE_FOR_BATCH_PAYMENT = List.of(
            Currency.Code.STRK,
            Currency.Code.USDC,
            Currency.Code.LORDS
    );
    private final AccountingRewardStoragePort accountingRewardStoragePort;
    private final InvoiceStoragePort invoiceStoragePort;
    private final AccountingFacadePort accountingFacadePort;
    private final MailNotificationPort mailNotificationPort;

    @NotNull
    private static Map<RewardId, Wallet> walletsPerRewardForNetwork(Map<RewardId, Invoice> rewardInvoices, Network network) {
        return rewardInvoices.entrySet().stream()
                .map(e -> Map.entry(e.getKey(), e.getValue().billingProfileSnapshot().wallet(network)))
                .filter(e -> e.getValue().isPresent())
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().get()));
    }

    @Override
    public List<BackofficeRewardView> searchForBatchPaymentByInvoiceIds(List<Invoice.Id> invoiceIds) {
        return accountingRewardStoragePort.searchRewards(List.of(Invoice.Status.APPROVED), invoiceIds)
                .stream()
                .filter(rewardView -> CURRENCY_CODES_AVAILABLE_FOR_BATCH_PAYMENT.contains(rewardView.money().currency().code()))
                // TODO: the following filter won't work when reward has multiple receipts
                .filter(rewardView -> isNull(rewardView.processedAt()) && rewardView.transactionReferences().isEmpty())
                .toList();
    }

    @Override
    public List<BackofficeRewardView> findByInvoiceId(Invoice.Id invoiceId) {
        return accountingRewardStoragePort.getInvoiceRewards(invoiceId);
    }

    @Override
    @Transactional
    public void markBatchPaymentAsPaid(BatchPayment.Id batchPaymentId, String transactionReference) {
        final var batchPayment = accountingRewardStoragePort.findBatchPayment(batchPaymentId)
                .orElseThrow(() -> OnlyDustException.notFound("Batch payment %s not found".formatted(batchPaymentId.value())));

        batchPayment.network().validateTransactionReference(transactionReference);

        final Map<RewardId, Invoice> rewardInvoices = batchPayment.invoices().stream()
                .flatMap(invoice -> invoice.rewards().stream().map(reward -> Map.entry(reward.id(), invoice)))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        final Map<RewardId, Wallet> wallets = walletsPerRewardForNetwork(rewardInvoices, batchPayment.network());

        batchPayment.rewards().forEach(reward -> {
            final var paymentReference = new SponsorAccount.PaymentReference(batchPayment.network(),
                    transactionReference,
                    rewardInvoices.get(reward.id()).billingProfileSnapshot().subject(),
                    wallets.get(reward.id()).address());

            accountingFacadePort.pay(reward.id(), reward.currency().id(), paymentReference);
        });

        final BatchPayment updatedBatchPayment = batchPayment.toBuilder()
                .status(BatchPayment.Status.PAID)
                .transactionHash(transactionReference)
                .build();
        accountingRewardStoragePort.saveBatchPayment(updatedBatchPayment);
    }

    @Override
    public Page<BatchPaymentDetailsView> findBatchPayments(int pageIndex, int pageSize) {
        return accountingRewardStoragePort.findBatchPaymentDetails(pageIndex, pageSize);
    }

    @Override
    public BatchPaymentDetailsView findBatchPaymentById(BatchPayment.Id batchPaymentId) {
        return accountingRewardStoragePort.findBatchPaymentDetailsById(batchPaymentId)
                .orElseThrow(() -> OnlyDustException.notFound("Batch payment details %s not found".formatted(batchPaymentId.value())));
    }

    @Override
    public List<BatchPaymentDetailsView> createBatchPaymentsForInvoices(List<Invoice.Id> invoiceIds) {

        final var invoices = invoiceStoragePort.getAll(invoiceIds);
        final Map<RewardId, Invoice> rewardInvoices = invoices.stream()
                .flatMap(invoice -> invoice.rewards().stream().map(reward -> Map.entry(reward.id(), invoice)))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        final Map<Network, List<PayableReward>> payableRewardsByNetwork = accountingFacadePort.getPayableRewards(rewardInvoices.keySet()).stream()
                .collect(groupingBy(r -> r.currency().network()));

        final var batchPayments = payableRewardsByNetwork
                .entrySet()
                .stream()
                .map(e -> {
                    final var network = e.getKey();
                    final var rewards = e.getValue();
                    rewards.sort(Comparator.comparing(r -> r.id().value()));
                    final Map<RewardId, Wallet> wallets = walletsPerRewardForNetwork(rewardInvoices, network);

                    return BatchPayment.builder()
                            .id(BatchPayment.Id.random())
                            .network(network)
                            .csv(BatchPaymentExporter.csv(rewards, wallets))
                            .invoices(invoices)
                            .rewards(rewards)
                            .build();
                })
                .toList();

        accountingRewardStoragePort.saveAll(batchPayments);
        return batchPayments.stream().map(batchPayment -> {
            final var rewards = accountingRewardStoragePort.findRewardsById(batchPayment.rewards().stream().map(PayableReward::id).collect(Collectors.toSet()));
            return BatchPaymentDetailsView.builder()
                    .batchPayment(batchPayment)
                    .rewardViews(rewards)
                    .build();
        }).toList();
    }

    @Override
    public Page<BackofficeRewardView> getRewards(int pageIndex, int pageSize,
                                                 List<RewardStatus> statuses,
                                                 Date fromRequestedAt, Date toRequestedAt,
                                                 Date fromProcessedAt, Date toProcessedAt) {
        Set<RewardStatus> sanitizedStatuses;
        if (statuses == null || statuses.isEmpty()) {
            sanitizedStatuses = EnumSet.allOf(RewardStatus.AsUser.class).stream().map(RewardStatus::new).collect(Collectors.toUnmodifiableSet());
        } else {
            sanitizedStatuses = statuses.stream().collect(Collectors.toUnmodifiableSet());
        }
        return accountingRewardStoragePort.findRewards(pageIndex, pageSize, sanitizedStatuses, fromRequestedAt, toRequestedAt, fromProcessedAt, toProcessedAt);
    }

    @Override
    public String exportRewardsCSV(List<RewardStatus> statuses,
                                   Date fromRequestedAt, Date toRequestedAt,
                                   Date fromProcessedAt, Date toProcessedAt) {
        final var rewards = accountingRewardStoragePort.findRewards(0, 1_000_000,
                statuses.stream().collect(Collectors.toUnmodifiableSet()), fromRequestedAt, toRequestedAt, fromProcessedAt, toProcessedAt);

        if (rewards.getTotalPageNumber() > 1) {
            throw badRequest("Too many rewards to export");
        }

        return RewardsExporter.csv(rewards.getContent());
    }

    @Override
    public void notifyAllNewPaidRewards() {
        final List<BackofficeRewardView> rewardViews = accountingRewardStoragePort.findPaidRewardsToNotify();
        for (Map.Entry<String, List<BackofficeRewardView>> listOfPaidRewardsMapToAdminEmail :
                rewardViews.stream().collect(groupingBy(rewardView -> rewardView.billingProfileAdmin().admins().get(0).email())).entrySet()) {
            mailNotificationPort.sendRewardsPaidMail(listOfPaidRewardsMapToAdminEmail.getKey(), listOfPaidRewardsMapToAdminEmail.getValue());
        }
        accountingRewardStoragePort.markRewardsAsPaymentNotified(rewardViews.stream()
                .map(BackofficeRewardView::id)
                .toList());
    }
}
