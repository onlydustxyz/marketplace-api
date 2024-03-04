package onlydust.com.marketplace.accounting.domain.service;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.accounting.domain.model.BatchPayment;
import onlydust.com.marketplace.accounting.domain.model.Currency;
import onlydust.com.marketplace.accounting.domain.model.Invoice;
import onlydust.com.marketplace.accounting.domain.port.in.AccountingRewardPort;
import onlydust.com.marketplace.accounting.domain.port.out.AccountingRewardStoragePort;
import onlydust.com.marketplace.accounting.domain.view.MoneyView;
import onlydust.com.marketplace.accounting.domain.view.PayableRewardWithPayoutInfoView;
import onlydust.com.marketplace.accounting.domain.view.RewardView;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import onlydust.com.marketplace.kernel.model.blockchain.Blockchain;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@AllArgsConstructor
public class RewardService implements AccountingRewardPort {

    private final AccountingRewardStoragePort accountingRewardStoragePort;

    @Override
    public List<RewardView> searchForApprovedInvoiceIds(List<Invoice.Id> invoiceIds) {
        return accountingRewardStoragePort.searchRewards(List.of(Invoice.Status.APPROVED), invoiceIds);
    }

    @Override
    public List<RewardView> findByInvoiceId(Invoice.Id invoiceId) {
        return accountingRewardStoragePort.getInvoiceRewards(invoiceId);
    }

    @Override
    public List<BatchPayment> createBatchPaymentsForInvoices(List<Invoice.Id> invoiceIds) {
        final List<PayableRewardWithPayoutInfoView> rewardViews = accountingRewardStoragePort.findPayableRewardsWithPayoutInfo(invoiceIds)
                .stream()
                .filter(r -> List.of(Currency.Code.STRK_STR, Currency.Code.USDC_STR, Currency.Code.LORDS_STR, Currency.Code.ETH_STR).contains(r.money().currencyCode()))
                .toList();
        if (rewardViews.isEmpty()) {
            return List.of();
        }
        final Map<String, List<PayableRewardWithPayoutInfoView>> ethereumRewardMapToCurrencyCode = new HashMap<>();
        final List<PayableRewardWithPayoutInfoView> starknetRewards = new ArrayList<>();
        for (PayableRewardWithPayoutInfoView rewardView : rewardViews) {
            final String currencyCode = rewardView.money().currencyCode();
            if (List.of(Currency.Code.USDC_STR, Currency.Code.LORDS_STR, Currency.Code.ETH_STR).contains(rewardView.money().currencyCode())) {
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
            accountingRewardStoragePort.createBatchPayment(batchPayment);
            batchPayments.add(batchPayment);
        }
        if (!starknetRewards.isEmpty()) {
            final BatchPayment batchPayment = buildStarknetBatchPayment(starknetRewards);
            accountingRewardStoragePort.createBatchPayment(batchPayment);
            batchPayments.add(batchPayment);
        }
        return batchPayments;
    }

    private BatchPayment buildEthereumBatchPayment(final Map<String, List<PayableRewardWithPayoutInfoView>> ethereumRewardMapToCurrencyCode) {
        final List<String[]> csvLines = new ArrayList<>();
        final Map<String, MoneyView> moneyViewByCurrencyCode = new HashMap<>();
        for (Map.Entry<String, List<PayableRewardWithPayoutInfoView>> rewardsByCurrencyCode : ethereumRewardMapToCurrencyCode.entrySet()) {
            for (PayableRewardWithPayoutInfoView payableRewardWithPayoutInfoView : rewardsByCurrencyCode.getValue()) {
                final String currencyCode = rewardsByCurrencyCode.getKey();
                switch (currencyCode) {
                    case Currency.Code.ETH_STR -> csvLines.add(new String[]{"native", "", payableRewardWithPayoutInfoView.wallet().address(),
                            payableRewardWithPayoutInfoView.money().amount().toString()});
                    case Currency.Code.USDC_STR ->
                            csvLines.add(new String[]{"erc20", "0xA0b86991c6218b36c1d19D4a2e9Eb0cE3606eB48", payableRewardWithPayoutInfoView.wallet().address(),
                                    payableRewardWithPayoutInfoView.money().amount().toString()});
                    case Currency.Code.LORDS_STR ->
                            csvLines.add(new String[]{"erc20", "0x686f2404e77Ab0d9070a46cdfb0B7feCDD2318b0", payableRewardWithPayoutInfoView.wallet().address(),
                                    payableRewardWithPayoutInfoView.money().amount().toString()});
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
                .rewardCount(Long.valueOf(ethereumRewardMapToCurrencyCode.values().stream().map(List::size).reduce(0, Integer::sum)))
                .moneys(moneyViewByCurrencyCode.values().stream().toList())
                .blockchain(Blockchain.ETHEREUM)
                .csv(csvLines.stream().map(lineData -> String.join(",", lineData)).collect(Collectors.joining("\n")))
                .build();
    }

    private BatchPayment buildStarknetBatchPayment(final List<PayableRewardWithPayoutInfoView> starknetRewards) {
        final List<String[]> csvLines = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;
        BigDecimal totalAmountDollarsEquivalent = BigDecimal.ZERO;
        for (PayableRewardWithPayoutInfoView starknetReward : starknetRewards) {
            totalAmount = totalAmount.add(starknetReward.money().amount());
            totalAmountDollarsEquivalent = totalAmountDollarsEquivalent.add(starknetReward.money().dollarsEquivalent());
            csvLines.add(new String[]{"erc20", "0xA0b86991c6218b36c1d19D4a2e9Eb0cE3606eB48", starknetReward.wallet().address(),
                    starknetReward.money().amount().toString()});
        }
        return BatchPayment.builder()
                .id(BatchPayment.Id.random())
                .rewardCount((long) starknetRewards.size())
                .blockchain(Blockchain.STARKNET)
                .csv(csvLines.stream().map(lineData -> String.join(",", lineData)).collect(Collectors.joining("\n")))
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
