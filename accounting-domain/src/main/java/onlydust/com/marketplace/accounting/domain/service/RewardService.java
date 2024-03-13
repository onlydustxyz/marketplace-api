package onlydust.com.marketplace.accounting.domain.service;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.accounting.domain.model.Currency;
import onlydust.com.marketplace.accounting.domain.model.*;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.Wallet;
import onlydust.com.marketplace.accounting.domain.port.in.AccountingFacadePort;
import onlydust.com.marketplace.accounting.domain.port.in.AccountingRewardPort;
import onlydust.com.marketplace.accounting.domain.port.out.AccountingRewardStoragePort;
import onlydust.com.marketplace.accounting.domain.port.out.MailNotificationPort;
import onlydust.com.marketplace.accounting.domain.view.BackofficeRewardView;
import onlydust.com.marketplace.accounting.domain.view.BatchPaymentDetailsView;
import onlydust.com.marketplace.accounting.domain.view.MoneyView;
import onlydust.com.marketplace.accounting.domain.view.RewardWithPayoutInfoView;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import onlydust.com.marketplace.kernel.model.RewardStatus;
import onlydust.com.marketplace.kernel.pagination.Page;
import org.jetbrains.annotations.NotNull;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.net.URI;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static java.util.stream.Collectors.groupingBy;
import static onlydust.com.marketplace.kernel.exception.OnlyDustException.badRequest;
import static onlydust.com.marketplace.kernel.exception.OnlyDustException.internalServerError;

@AllArgsConstructor
public class RewardService implements AccountingRewardPort {

    private final AccountingRewardStoragePort accountingRewardStoragePort;
    private final AccountingFacadePort accountingFacadePort;
    private final MailNotificationPort mailNotificationPort;
    private static final List<String> CURRENCY_CODES_AVAILABLE_FOR_BATCH_PAYMENT = List.of(Currency.Code.STRK_STR, Currency.Code.USDC_STR,
            Currency.Code.LORDS_STR);

    @Override
    public List<BackofficeRewardView> searchForBatchPaymentByInvoiceIds(List<Invoice.Id> invoiceIds) {
        return accountingRewardStoragePort.searchRewards(List.of(Invoice.Status.APPROVED), invoiceIds)
                .stream()
                .filter(rewardView -> CURRENCY_CODES_AVAILABLE_FOR_BATCH_PAYMENT.contains(rewardView.money().currencyCode()))
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
    public void markBatchPaymentAsPaid(BatchPayment.Id batchPaymentId, String transactionHash) {
        //TODO
        //TODO
        //TODO
//        final BatchPayment batchPayment = accountingRewardStoragePort.findBatchPayment(batchPaymentId)
//                .orElseThrow(() -> OnlyDustException.notFound("Batch payment %s not found".formatted(batchPaymentId.value())));
//        if (!transactionHash.startsWith("0x")) {
//            throw badRequest("Wrong transaction hash format %s".formatted(transactionHash));
//        }
//
//        final List<PayableRewardWithPayoutInfoView> rewardViews = accountingRewardStoragePort.findPayableRewardsWithPayoutInfoForBatchPayment(batchPaymentId);
//        for (PayableRewardWithPayoutInfoView rewardView : rewardViews) {
//            oldRewardStoragePort.markRewardAsPaid(rewardView, transactionHash);
//        }
//        final BatchPayment updatedBatchPayment = batchPayment.toBuilder()
//                .status(BatchPayment.Status.PAID)
//                .transactionHash(transactionHash)
//                .build();
//        accountingRewardStoragePort.saveBatchPayment(updatedBatchPayment);
    }

    @Override
    public Page<BatchPayment> findBatchPayments(int pageIndex, int pageSize) {
        return accountingRewardStoragePort.findBatchPayments(pageIndex, pageSize);
    }

    @Override
    public BatchPaymentDetailsView findBatchPaymentById(BatchPayment.Id batchPaymentId) {
        return accountingRewardStoragePort.findBatchPaymentDetailsById(batchPaymentId)
                .orElseThrow(() -> OnlyDustException.notFound("Batch payment details %s not found".formatted(batchPaymentId.value())));
    }

    @Override
    public List<BatchPayment> createBatchPaymentsForInvoices(List<Invoice.Id> invoiceIds) {
        final Map<RewardId, RewardWithPayoutInfoView> rewardsWithPayoutInfo = accountingRewardStoragePort.getRewardWithPayoutInfoOfInvoices(invoiceIds)
                .stream().collect(Collectors.toMap(RewardWithPayoutInfoView::id, Function.identity()));

        final Map<Network, List<PayableReward>> payableRewardsByNetwork = accountingFacadePort.getPayableRewards(rewardsWithPayoutInfo.keySet()).stream()
                .collect(groupingBy(r -> r.currency().network()));

        return payableRewardsByNetwork
                .entrySet()
                .stream()
                .map(e -> {
                    final var network = e.getKey();
                    final var rewards = e.getValue();
                    final var moneys = totalAmountsPerCurrency(rewards, rewardsWithPayoutInfo);
                    final Map<RewardId, Wallet> wallets = walletsPerRewardForNetwork(rewardsWithPayoutInfo, network);

                    final var batchPayment = BatchPayment.builder()
                            .id(BatchPayment.Id.random())
                            .moneys(moneys)
                            .network(network)
                            .csv(BatchPaymentExporter.csv(rewards, wallets))
                            .rewardIds(rewards.stream().map(PayableReward::id).toList())
                            .build();
                    accountingRewardStoragePort.saveBatchPayment(batchPayment);
                    return batchPayment;
                })
                .toList();
    }

    @NotNull
    private static Map<RewardId, Wallet> walletsPerRewardForNetwork(Map<RewardId, RewardWithPayoutInfoView> rewardsWithPayoutInfo, Network network) {
        return rewardsWithPayoutInfo.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey,
                        e -> e.getValue().payoutInfo().orElseThrow(() -> internalServerError("No payout info for reward %s".formatted(e.getKey())))
                                .wallet(network).orElseThrow(() -> internalServerError("No wallet for reward %s on network %s".formatted(e.getKey(), network)))));
    }

    @NotNull
    private static List<MoneyView> totalAmountsPerCurrency(List<PayableReward> rewards, Map<RewardId, RewardWithPayoutInfoView> rewardsWithPayoutInfo) {
        return rewards.stream()
                .collect(groupingBy(PayableReward::currency))
                .entrySet()
                .stream()
                .map(e -> new MoneyView(
                        e.getValue().stream().map(PayableReward::amount).reduce(PositiveAmount::add).orElseThrow().getValue(),
                        e.getKey().name(),
                        e.getKey().code().toString(),
                        e.getKey().logoUrl().map(URI::toString).orElse(null),
                        e.getValue().stream().map(r -> r.amount().getValue().multiply(rewardsWithPayoutInfo.get(r.id()).usdConversionRate()))
                                .reduce(BigDecimal::add).orElseThrow()
                )).toList();
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
