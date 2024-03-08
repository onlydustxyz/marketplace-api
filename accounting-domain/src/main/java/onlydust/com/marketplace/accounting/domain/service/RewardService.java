package onlydust.com.marketplace.accounting.domain.service;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.accounting.domain.model.BatchPayment;
import onlydust.com.marketplace.accounting.domain.model.Currency;
import onlydust.com.marketplace.accounting.domain.model.Invoice;
import onlydust.com.marketplace.accounting.domain.model.RewardId;
import onlydust.com.marketplace.accounting.domain.port.in.AccountingRewardPort;
import onlydust.com.marketplace.accounting.domain.port.out.AccountingRewardStoragePort;
import onlydust.com.marketplace.accounting.domain.port.out.MailNotificationPort;
import onlydust.com.marketplace.accounting.domain.port.out.OldRewardStoragePort;
import onlydust.com.marketplace.accounting.domain.view.*;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import onlydust.com.marketplace.kernel.model.blockchain.Blockchain;
import onlydust.com.marketplace.kernel.pagination.Page;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static onlydust.com.marketplace.kernel.exception.OnlyDustException.badRequest;

@AllArgsConstructor
public class RewardService implements AccountingRewardPort {

    private final AccountingRewardStoragePort accountingRewardStoragePort;
    private final OldRewardStoragePort oldRewardStoragePort;
    private final MailNotificationPort mailNotificationPort;
    private static final List<String> CURRENCY_CODES_AVAILABLE_FOR_BATCH_PAYMENT = List.of(Currency.Code.STRK_STR, Currency.Code.USDC_STR,
            Currency.Code.LORDS_STR);

    @Override
    public List<RewardView> searchForBatchPaymentByInvoiceIds(List<Invoice.Id> invoiceIds) {
        return accountingRewardStoragePort.searchRewards(List.of(Invoice.Status.APPROVED), invoiceIds)
                .stream()
                .filter(rewardView -> CURRENCY_CODES_AVAILABLE_FOR_BATCH_PAYMENT.contains(rewardView.money().currencyCode()))
                // TODO: the following filter won't work when reward has multiple receipts
                .filter(rewardView -> isNull(rewardView.processedAt()) && rewardView.transactionReferences().isEmpty())
                .toList();
    }

    @Override
    public List<RewardView> findByInvoiceId(Invoice.Id invoiceId) {
        return accountingRewardStoragePort.getInvoiceRewards(invoiceId);
    }

    @Override
    @Transactional
    public void markBatchPaymentAsPaid(BatchPayment.Id batchPaymentId, String transactionHash) {
        final BatchPayment batchPayment = accountingRewardStoragePort.findBatchPayment(batchPaymentId)
                .orElseThrow(() -> OnlyDustException.notFound("Batch payment %s not found".formatted(batchPaymentId.value())));
        if (!transactionHash.startsWith("0x")) {
            throw badRequest("Wrong transaction hash format %s".formatted(transactionHash));
        }

        final List<PayableRewardWithPayoutInfoView> rewardViews = accountingRewardStoragePort.findPayableRewardsWithPayoutInfoForBatchPayment(batchPaymentId);
        for (PayableRewardWithPayoutInfoView rewardView : rewardViews) {
            oldRewardStoragePort.markRewardAsPaid(rewardView, transactionHash);
        }
        final BatchPayment updatedBatchPayment = batchPayment.toBuilder()
                .status(BatchPayment.Status.PAID)
                .transactionHash(transactionHash)
                .build();
        accountingRewardStoragePort.saveBatchPayment(updatedBatchPayment);
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
        final List<PayableRewardWithPayoutInfoView> rewardViews = accountingRewardStoragePort.findPayableRewardsWithPayoutInfoForInvoices(invoiceIds)
                .stream()
                .filter(r -> CURRENCY_CODES_AVAILABLE_FOR_BATCH_PAYMENT.contains(r.money().currencyCode()))
                .toList();
        if (rewardViews.isEmpty()) {
            return List.of();
        }
        final Map<String, List<PayableRewardWithPayoutInfoView>> ethereumRewardMapToCurrencyCode = new HashMap<>();
        final List<PayableRewardWithPayoutInfoView> starknetRewards = new ArrayList<>();
        for (PayableRewardWithPayoutInfoView rewardView : rewardViews) {
            final String currencyCode = rewardView.money().currencyCode();
            if (List.of(Currency.Code.USDC_STR, Currency.Code.LORDS_STR).contains(rewardView.money().currencyCode())) {
                if (ethereumRewardMapToCurrencyCode.containsKey(currencyCode)) {
                    ethereumRewardMapToCurrencyCode.get(currencyCode).add(rewardView);
                } else {
                    final List<PayableRewardWithPayoutInfoView> rewardViewsForCurrencyCode = new ArrayList<>();
                    rewardViewsForCurrencyCode.add(rewardView);
                    ethereumRewardMapToCurrencyCode.put(currencyCode, rewardViewsForCurrencyCode);
                }
            } else if (rewardView.money().currencyCode().equals(Currency.Code.STRK_STR)) {
                starknetRewards.add(rewardView);
            } else {
                throw OnlyDustException.forbidden("Currency %s is not supported in batch payment".formatted(currencyCode));
            }

        }
        final List<BatchPayment> batchPayments = new ArrayList<>();
        if (!ethereumRewardMapToCurrencyCode.isEmpty()) {
            final BatchPayment batchPayment = buildEthereumBatchPayment(ethereumRewardMapToCurrencyCode);
            accountingRewardStoragePort.saveBatchPayment(batchPayment);
            batchPayments.add(batchPayment);
        }
        if (!starknetRewards.isEmpty()) {
            final BatchPayment batchPayment = buildStarknetBatchPayment(starknetRewards);
            accountingRewardStoragePort.saveBatchPayment(batchPayment);
            batchPayments.add(batchPayment);
        }
        return batchPayments;
    }

    @Override
    public Page<RewardDetailsView> getRewards(int pageIndex, int pageSize,
                                              List<RewardDetailsView.Status> statuses,
                                              Date fromRequestedAt, Date toRequestedAt,
                                              Date fromProcessedAt, Date toProcessedAt) {
        Set<RewardDetailsView.Status> sanitizedStatuses;
        if (statuses == null || statuses.isEmpty()) {
            sanitizedStatuses = EnumSet.allOf(RewardDetailsView.Status.class).stream().collect(Collectors.toUnmodifiableSet());
        } else {
            sanitizedStatuses = statuses.stream().collect(Collectors.toUnmodifiableSet());
        }
        return accountingRewardStoragePort.findRewards(pageIndex, pageSize, sanitizedStatuses, fromRequestedAt, toRequestedAt, fromProcessedAt, toProcessedAt);
    }

    @Override
    public String exportRewardsCSV(List<RewardDetailsView.Status> statuses,
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
        final List<RewardView> rewardViews = accountingRewardStoragePort.findPaidRewardsToNotify();
        for (Map.Entry<String, List<RewardView>> listOfPaidRewardsMapToAdminEmail :
                rewardViews.stream().collect(Collectors.groupingBy(rewardView -> rewardView.billingProfileAdmin().admins().get(0).email())).entrySet()) {
            mailNotificationPort.sendRewardsPaidMail(listOfPaidRewardsMapToAdminEmail.getKey(), listOfPaidRewardsMapToAdminEmail.getValue());
        }
        accountingRewardStoragePort.markRewardsAsPaymentNotified(rewardViews.stream()
                .map(RewardView::id)
                .map(RewardId::of)
                .toList());
    }

    private BatchPayment buildEthereumBatchPayment(final Map<String, List<PayableRewardWithPayoutInfoView>> ethereumRewardMapToCurrencyCode) {
        final List<String[]> csvLines = new ArrayList<>();
        final Map<String, MoneyView> moneyViewByCurrencyCode = new HashMap<>();
        for (Map.Entry<String, List<PayableRewardWithPayoutInfoView>> rewardsByCurrencyCode : ethereumRewardMapToCurrencyCode.entrySet()) {
            for (PayableRewardWithPayoutInfoView payableRewardWithPayoutInfoView : rewardsByCurrencyCode.getValue()) {
                final String currencyCode = rewardsByCurrencyCode.getKey();
                switch (currencyCode) {
                    case Currency.Code.USDC_STR ->
                            csvLines.add(new String[]{"erc20", "0xA0b86991c6218b36c1d19D4a2e9Eb0cE3606eB48", payableRewardWithPayoutInfoView.wallet().address(),
                                    payableRewardWithPayoutInfoView.money().amount().toString(), " "});
                    case Currency.Code.LORDS_STR ->
                            csvLines.add(new String[]{"erc20", "0x686f2404e77Ab0d9070a46cdfb0B7feCDD2318b0", payableRewardWithPayoutInfoView.wallet().address(),
                                    payableRewardWithPayoutInfoView.money().amount().toString(), " "});
                }
                if (moneyViewByCurrencyCode.containsKey(currencyCode)) {
                    final MoneyView moneyView = moneyViewByCurrencyCode.get(currencyCode);
                    moneyViewByCurrencyCode.replace(currencyCode, moneyView.toBuilder()
                            .dollarsEquivalent(moneyView.dollarsEquivalent().add(payableRewardWithPayoutInfoView.money().dollarsEquivalent()))
                            .amount(moneyView.amount().add(payableRewardWithPayoutInfoView.money().amount()))
                            .build());
                } else {
                    moneyViewByCurrencyCode.put(currencyCode, payableRewardWithPayoutInfoView.money());
                }
            }
        }
        return BatchPayment.builder()
                .id(BatchPayment.Id.random())
                .moneys(moneyViewByCurrencyCode.values().stream().toList())
                .blockchain(Blockchain.ETHEREUM)
                .csv(csvLines.stream().map(lineData -> String.join(",", lineData)).collect(Collectors.joining("\n")))
                .rewardIds(ethereumRewardMapToCurrencyCode.values().stream()
                        .flatMap(payableRewardWithPayoutInfoViews -> payableRewardWithPayoutInfoViews.stream()
                                .map(r -> RewardId.of(r.id())).toList().stream()).toList())
                .build();
    }

    private BatchPayment buildStarknetBatchPayment(final List<PayableRewardWithPayoutInfoView> starknetRewards) {
        final List<String[]> csvLines = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;
        BigDecimal totalAmountDollarsEquivalent = BigDecimal.ZERO;
        for (PayableRewardWithPayoutInfoView starknetReward : starknetRewards) {
            totalAmount = totalAmount.add(starknetReward.money().amount());
            totalAmountDollarsEquivalent = totalAmountDollarsEquivalent.add(starknetReward.money().dollarsEquivalent());
            csvLines.add(new String[]{"erc20", "0x04718f5a0fc34cc1af16a1cdee98ffb20c31f5cd61d6ab07201858f4287c938d", starknetReward.wallet().address(),
                    starknetReward.money().amount().toString(), " "});
        }
        return BatchPayment.builder()
                .id(BatchPayment.Id.random())
                .blockchain(Blockchain.STARKNET)
                .csv(csvLines.stream().map(lineData -> String.join(",", lineData)).collect(Collectors.joining("\n")))
                .rewardIds(starknetRewards.stream().map(r -> RewardId.of(r.id())).toList())
                .moneys(List.of(MoneyView.builder()
                        .amount(totalAmount)
                        .dollarsEquivalent(totalAmountDollarsEquivalent)
                        .currencyLogoUrl(starknetRewards.get(0).money().currencyLogoUrl())
                        .currencyName(starknetRewards.get(0).money().currencyName())
                        .currencyCode(starknetRewards.get(0).money().currencyCode())
                        .build())
                )
                .build();
    }
}
