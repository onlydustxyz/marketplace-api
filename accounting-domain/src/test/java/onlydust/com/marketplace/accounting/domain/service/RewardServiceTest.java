package onlydust.com.marketplace.accounting.domain.service;

import com.github.javafaker.Faker;
import onlydust.com.marketplace.accounting.domain.model.BatchPayment;
import onlydust.com.marketplace.accounting.domain.model.Currency;
import onlydust.com.marketplace.accounting.domain.model.Invoice;
import onlydust.com.marketplace.accounting.domain.model.Network;
import onlydust.com.marketplace.accounting.domain.port.out.AccountingRewardStoragePort;
import onlydust.com.marketplace.accounting.domain.view.MoneyView;
import onlydust.com.marketplace.accounting.domain.view.PayableRewardWithPayoutInfoView;
import onlydust.com.marketplace.kernel.model.blockchain.Blockchain;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class RewardServiceTest {

    private final Faker faker = new Faker();

    @Test
    void should_generate_batch_payment_given_all_currencies() {
        // Given
        final AccountingRewardStoragePort accountingRewardStoragePort = mock(AccountingRewardStoragePort.class);
        final RewardService rewardService = new RewardService(accountingRewardStoragePort);
        final List<Invoice.Id> invoiceIds = List.of(Invoice.Id.random(), Invoice.Id.random());

        // When
        final PayableRewardWithPayoutInfoView strk1 = PayableRewardWithPayoutInfoView.builder()
                .id(UUID.randomUUID())
                .money(MoneyView.builder()
                        .amount(BigDecimal.valueOf(11.22))
                        .dollarsEquivalent(BigDecimal.valueOf(311.22))
                        .currencyCode(Currency.Code.STRK_STR)
                        .currencyLogoUrl("https://stark.logo")
                        .currencyName("strk")
                        .build())
                .wallet(new Invoice.Wallet(Network.STARKNET, faker.internet().macAddress()))
                .build();
        final PayableRewardWithPayoutInfoView strk2 = PayableRewardWithPayoutInfoView.builder()
                .id(UUID.randomUUID())
                .money(MoneyView.builder()
                        .amount(BigDecimal.valueOf(405.38))
                        .dollarsEquivalent(BigDecimal.valueOf(5960))
                        .currencyCode(Currency.Code.STRK_STR)
                        .currencyLogoUrl("https://stark.logo")
                        .currencyName("strk")
                        .build())
                .wallet(new Invoice.Wallet(Network.STARKNET, faker.internet().macAddress()))
                .build();
        final PayableRewardWithPayoutInfoView eth = PayableRewardWithPayoutInfoView.builder()
                .id(UUID.randomUUID())
                .money(MoneyView.builder()
                        .amount(BigDecimal.valueOf(67.54))
                        .dollarsEquivalent(BigDecimal.valueOf(8909))
                        .currencyCode(Currency.Code.ETH_STR)
                        .currencyLogoUrl("https://eth.logo")
                        .currencyName("eth")
                        .build())
                .wallet(new Invoice.Wallet(Network.ETHEREUM, faker.internet().macAddress()))
                .build();
        final PayableRewardWithPayoutInfoView lords = PayableRewardWithPayoutInfoView.builder()
                .id(UUID.randomUUID())
                .money(MoneyView.builder()
                        .amount(BigDecimal.valueOf(105.4))
                        .dollarsEquivalent(BigDecimal.valueOf(6885))
                        .currencyCode(Currency.Code.LORDS_STR)
                        .currencyLogoUrl("https://lords.logo")
                        .currencyName("op")
                        .build())
                .wallet(new Invoice.Wallet(Network.ETHEREUM, faker.internet().macAddress()))
                .build();
        final PayableRewardWithPayoutInfoView usdc1 = PayableRewardWithPayoutInfoView.builder()
                .id(UUID.randomUUID())
                .money(MoneyView.builder()
                        .amount(BigDecimal.valueOf(23.9))
                        .dollarsEquivalent(BigDecimal.valueOf(2234))
                        .currencyCode(Currency.Code.USDC_STR)
                        .currencyLogoUrl("https://usdc.logo")
                        .currencyName("usdc")
                        .build())
                .wallet(new Invoice.Wallet(Network.ETHEREUM, faker.internet().macAddress()))
                .build();
        final PayableRewardWithPayoutInfoView usdc2 = PayableRewardWithPayoutInfoView.builder()
                .id(UUID.randomUUID())
                .money(MoneyView.builder()
                        .amount(BigDecimal.valueOf(95666))
                        .dollarsEquivalent(BigDecimal.valueOf(95623))
                        .currencyCode(Currency.Code.USDC_STR)
                        .currencyLogoUrl("https://usdc.logo")
                        .currencyName("usdc")
                        .build())
                .wallet(new Invoice.Wallet(Network.ETHEREUM, faker.internet().macAddress()))
                .build();
        when(accountingRewardStoragePort.findPayableRewardsWithPayoutInfo(invoiceIds))
                .thenReturn(List.of(
                        strk1,
                        strk2,
                        eth,
                        lords,
                        usdc1,
                        usdc2
                ));
        final List<BatchPayment> batchPaymentsForInvoices = rewardService.createBatchPaymentsForInvoices(invoiceIds);

        // Then
        assertEquals(2, batchPaymentsForInvoices.size());
        final BatchPayment starknetBatchPayment =
                batchPaymentsForInvoices.stream().filter(batchPayment -> batchPayment.blockchain().equals(Blockchain.STARKNET)).findFirst().orElseThrow();
        verify(accountingRewardStoragePort).createBatchPayment(starknetBatchPayment);
        assertEquals(2, starknetBatchPayment.rewardCount());
        assertEquals(1, starknetBatchPayment.moneys().size());
        assertEquals(strk1.money().amount().add(strk2.money().amount()), starknetBatchPayment.moneys().get(0).amount());
        assertEquals(strk1.money().dollarsEquivalent().add(strk2.money().dollarsEquivalent()), starknetBatchPayment.moneys().get(0).dollarsEquivalent());
        assertEquals(strk1.money().currencyName(), starknetBatchPayment.moneys().get(0).currencyName());
        assertEquals(strk1.money().currencyCode(), starknetBatchPayment.moneys().get(0).currencyCode());
        assertEquals(strk1.money().currencyLogoUrl(), starknetBatchPayment.moneys().get(0).currencyLogoUrl());
        assertEquals("""
                        erc20,0xA0b86991c6218b36c1d19D4a2e9Eb0cE3606eB48,%s,%s
                        erc20,0xA0b86991c6218b36c1d19D4a2e9Eb0cE3606eB48,%s,%s"""
                        .formatted(strk1.wallet().address(), strk1.money().amount(),
                                strk2.wallet().address(), strk2.money().amount()),
                starknetBatchPayment.csv());

        final BatchPayment ethereumBatchPayment =
                batchPaymentsForInvoices.stream().filter(batchPayment -> batchPayment.blockchain().equals(Blockchain.ETHEREUM)).findFirst().orElseThrow();
        verify(accountingRewardStoragePort).createBatchPayment(ethereumBatchPayment);
        assertEquals(4, ethereumBatchPayment.rewardCount());
        assertEquals(3, ethereumBatchPayment.moneys().size());
        assertEquals(eth.money(),
                ethereumBatchPayment.moneys().stream().filter(moneyView -> moneyView.currencyCode().equals(Currency.Code.ETH_STR)).findFirst().orElseThrow());
        assertEquals(lords.money(),
                ethereumBatchPayment.moneys().stream().filter(moneyView -> moneyView.currencyCode().equals(Currency.Code.LORDS_STR)).findFirst().orElseThrow());
        assertEquals(usdc1.money().toBuilder()
                        .dollarsEquivalent(usdc1.money().dollarsEquivalent().add(usdc2.money().dollarsEquivalent()))
                        .amount(usdc1.money().amount().add(usdc2.money().amount()))
                        .build()
                ,
                ethereumBatchPayment.moneys().stream().filter(moneyView -> moneyView.currencyCode().equals(Currency.Code.USDC_STR)).findFirst().orElseThrow());
        assertEquals("""
                native,,%s,%s
                erc20,0x686f2404e77Ab0d9070a46cdfb0B7feCDD2318b0,%s,%s
                erc20,0xA0b86991c6218b36c1d19D4a2e9Eb0cE3606eB48,%s,%s
                erc20,0xA0b86991c6218b36c1d19D4a2e9Eb0cE3606eB48,%s,%s"""
                .formatted(eth.wallet().address(), eth.money().amount(), lords.wallet().address(), lords.money().amount().toString(), usdc1.wallet().address(),
                        usdc1.money().amount().toString(), usdc2.wallet().address(), usdc2.money().amount().toString()), ethereumBatchPayment.csv());
    }
}
