package onlydust.com.marketplace.accounting.domain.service;

import com.github.javafaker.Faker;
import onlydust.com.marketplace.accounting.domain.model.BatchPayment;
import onlydust.com.marketplace.accounting.domain.model.Currency;
import onlydust.com.marketplace.accounting.domain.model.Invoice;
import onlydust.com.marketplace.accounting.domain.model.Network;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.BillingProfile;
import onlydust.com.marketplace.accounting.domain.port.out.AccountingRewardStoragePort;
import onlydust.com.marketplace.accounting.domain.port.out.OldRewardStoragePort;
import onlydust.com.marketplace.accounting.domain.view.MoneyView;
import onlydust.com.marketplace.accounting.domain.view.PayableRewardWithPayoutInfoView;
import onlydust.com.marketplace.accounting.domain.view.RewardView;
import onlydust.com.marketplace.accounting.domain.view.ShortBillingProfileAdminView;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import onlydust.com.marketplace.kernel.model.blockchain.Blockchain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

public class RewardServiceTest {

    private final Faker faker = new Faker();
    private final AccountingRewardStoragePort accountingRewardStoragePort = mock(AccountingRewardStoragePort.class);
    private final OldRewardStoragePort oldRewardStoragePort = mock(OldRewardStoragePort.class);
    private final RewardService rewardService = new RewardService(accountingRewardStoragePort, oldRewardStoragePort);

    @BeforeEach
    void setUp() {
        reset(accountingRewardStoragePort, oldRewardStoragePort);
    }

    @Test
    void should_generate_batch_payment_given_all_currencies() {
        // Given
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
        when(accountingRewardStoragePort.findPayableRewardsWithPayoutInfoForInvoices(invoiceIds))
                .thenReturn(List.of(
                        PayableRewardWithPayoutInfoView.builder()
                                .id(UUID.randomUUID())
                                .money(MoneyView.builder()
                                        .amount(BigDecimal.valueOf(1))
                                        .dollarsEquivalent(BigDecimal.valueOf(1))
                                        .currencyCode(Currency.Code.USD_STR)
                                        .currencyLogoUrl("https://usd.logo")
                                        .currencyName("usd")
                                        .build())
                                .wallet(null)
                                .build(),
                        PayableRewardWithPayoutInfoView.builder()
                                .id(UUID.randomUUID())
                                .money(MoneyView.builder()
                                        .amount(BigDecimal.valueOf(1))
                                        .dollarsEquivalent(BigDecimal.valueOf(1))
                                        .currencyCode(Currency.Code.EUR_STR)
                                        .currencyLogoUrl("https://eur.logo")
                                        .currencyName("eur")
                                        .build())
                                .wallet(null)
                                .build(),
                        PayableRewardWithPayoutInfoView.builder()
                                .id(UUID.randomUUID())
                                .money(MoneyView.builder()
                                        .amount(BigDecimal.valueOf(1))
                                        .dollarsEquivalent(BigDecimal.valueOf(1))
                                        .currencyCode(Currency.Code.OP_STR)
                                        .currencyLogoUrl("https://op.logo")
                                        .currencyName("OP")
                                        .build())
                                .wallet(null)
                                .build(),
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
        verify(accountingRewardStoragePort).saveBatchPayment(starknetBatchPayment);
        assertEquals(2, starknetBatchPayment.rewardIds().size());
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
        verify(accountingRewardStoragePort).saveBatchPayment(ethereumBatchPayment);
        assertEquals(3, ethereumBatchPayment.rewardIds().size());
        assertEquals(2, ethereumBatchPayment.moneys().size());
        assertEquals(0,
                ethereumBatchPayment.moneys().stream().filter(moneyView -> moneyView.currencyCode().equals(Currency.Code.ETH_STR)).toList().size());
        assertEquals(lords.money(),
                ethereumBatchPayment.moneys().stream().filter(moneyView -> moneyView.currencyCode().equals(Currency.Code.LORDS_STR)).findFirst().orElseThrow());
        assertEquals(usdc1.money().toBuilder()
                        .dollarsEquivalent(usdc1.money().dollarsEquivalent().add(usdc2.money().dollarsEquivalent()))
                        .amount(usdc1.money().amount().add(usdc2.money().amount()))
                        .build()
                ,
                ethereumBatchPayment.moneys().stream().filter(moneyView -> moneyView.currencyCode().equals(Currency.Code.USDC_STR)).findFirst().orElseThrow());
        assertEquals("""
                erc20,0x686f2404e77Ab0d9070a46cdfb0B7feCDD2318b0,%s,%s
                erc20,0xA0b86991c6218b36c1d19D4a2e9Eb0cE3606eB48,%s,%s
                erc20,0xA0b86991c6218b36c1d19D4a2e9Eb0cE3606eB48,%s,%s"""
                .formatted(lords.wallet().address(), lords.money().amount().toString(), usdc1.wallet().address(),
                        usdc1.money().amount().toString(), usdc2.wallet().address(), usdc2.money().amount().toString()), ethereumBatchPayment.csv());
    }

    @Test
    void should_raise_not_found_exception_given_not_existing_batch_payment() {
        // Given
        final BatchPayment.Id batchPaymentId = BatchPayment.Id.random();

        // When
        when(accountingRewardStoragePort.findBatchPayment(batchPaymentId))
                .thenReturn(Optional.empty());
        Exception exception = null;
        try {
            rewardService.markBatchPaymentAsPaid(batchPaymentId, faker.random().hex());
        } catch (Exception e) {
            exception = e;
        }

        // Then
        assertTrue(exception instanceof OnlyDustException);
        assertEquals(404, ((OnlyDustException) exception).getStatus());
        assertEquals("Batch payment %s not found".formatted(batchPaymentId.value()), exception.getMessage());
    }


    @Test
    void should_raise_wrong_transaction_hash_exception() {
        // Given
        final BatchPayment.Id batchPaymentId = BatchPayment.Id.random();
        final String transactionHash = faker.rickAndMorty().character();

        // When
        when(accountingRewardStoragePort.findBatchPayment(batchPaymentId))
                .thenReturn(Optional.of(BatchPayment.builder()
                        .csv("")
                        .id(BatchPayment.Id.random())
                        .moneys(List.of())
                        .blockchain(Blockchain.STARKNET)
                        .rewardIds(List.of())
                        .build()));
        Exception exception = null;
        try {
            rewardService.markBatchPaymentAsPaid(batchPaymentId, transactionHash);
        } catch (Exception e) {
            exception = e;
        }

        // Then
        assertTrue(exception instanceof OnlyDustException);
        assertEquals(400, ((OnlyDustException) exception).getStatus());
        assertEquals("Wrong transaction hash format %s".formatted(transactionHash), exception.getMessage());
    }

    @Test
    void should_update_batch_payment_and_linked_rewards_with_transaction_hash() {
        // Given
        final BatchPayment.Id batchPaymentId = BatchPayment.Id.random();
        final String transactionHash = "0x" + faker.random().hex();
        final BatchPayment batchPayment = BatchPayment.builder()
                .id(batchPaymentId)
                .moneys(List.of())
                .blockchain(Blockchain.STARKNET)
                .csv(faker.gameOfThrones().character())
                .rewardIds(List.of())
                .build();
        final List<PayableRewardWithPayoutInfoView> payableRewardWithPayoutInfoViews = List.of(
                PayableRewardWithPayoutInfoView.builder()
                        .id(UUID.randomUUID())
                        .money(MoneyView.builder()
                                .amount(BigDecimal.ONE)
                                .currencyCode(faker.gameOfThrones().city())
                                .currencyName(faker.gameOfThrones().dragon())
                                .currencyLogoUrl(faker.gameOfThrones().quote())
                                .build())
                        .build(),
                PayableRewardWithPayoutInfoView.builder()
                        .id(UUID.randomUUID())
                        .money(MoneyView.builder()
                                .amount(BigDecimal.ONE)
                                .currencyCode(faker.gameOfThrones().city())
                                .currencyName(faker.gameOfThrones().dragon())
                                .currencyLogoUrl(faker.gameOfThrones().quote())
                                .build())
                        .build()
        );
        final BatchPayment updatedBatchPayment = batchPayment.toBuilder()
                .status(BatchPayment.Status.PAID)
                .transactionHash(transactionHash)
                .build();


        // When
        when(accountingRewardStoragePort.findBatchPayment(batchPaymentId))
                .thenReturn(Optional.of(batchPayment));
        when(accountingRewardStoragePort.findPayableRewardsWithPayoutInfoForBatchPayment(batchPaymentId))
                .thenReturn(payableRewardWithPayoutInfoViews);
        rewardService.markBatchPaymentAsPaid(batchPaymentId, transactionHash);

        // Then
        verify(oldRewardStoragePort).markRewardAsPaid(payableRewardWithPayoutInfoViews.get(0), transactionHash);
        verify(oldRewardStoragePort).markRewardAsPaid(payableRewardWithPayoutInfoViews.get(1), transactionHash);
        verify(accountingRewardStoragePort).saveBatchPayment(updatedBatchPayment);
    }

    @Test
    void should_search_for_batch_payment() {
        // Given
        final List<Invoice.Id> invoiceIds = List.of(Invoice.Id.of(UUID.randomUUID()));

        // When
        when(accountingRewardStoragePort.searchRewards(List.of(Invoice.Status.APPROVED), invoiceIds))
                .thenReturn(List.of(
                        generateRewardStubForCurrency(Currency.Code.ETH_STR),
                        generateRewardStubForCurrency(Currency.Code.EUR_STR),
                        generateRewardStubForCurrency(Currency.Code.OP_STR),
                        generateRewardStubForCurrency(Currency.Code.USD_STR),
                        generateRewardStubForCurrency(Currency.Code.USDC_STR),
                        generateRewardStubForCurrency(Currency.Code.APT_STR),
                        generateRewardStubForCurrency(Currency.Code.LORDS_STR),
                        generateRewardStubForCurrency(Currency.Code.STRK_STR)
                ));
        final List<RewardView> rewardViews = rewardService.searchForBatchPaymentByInvoiceIds(invoiceIds);

        // Then
        assertEquals(3, rewardViews.size());
        assertEquals(Currency.Code.USDC_STR, rewardViews.get(0).money().currencyCode());
        assertEquals(Currency.Code.LORDS_STR, rewardViews.get(1).money().currencyCode());
        assertEquals(Currency.Code.STRK_STR, rewardViews.get(2).money().currencyCode());
    }

    private RewardView generateRewardStubForCurrency(final String currencyCode) {
        return RewardView.builder()
                .id(UUID.randomUUID())
                .billingProfileAdmin(ShortBillingProfileAdminView.builder()
                        .adminEmail(faker.gameOfThrones().character())
                        .billingProfileName(faker.gameOfThrones().character())
                        .adminGithubLogin(faker.gameOfThrones().character())
                        .adminName(faker.gameOfThrones().character())
                        .adminEmail(faker.gameOfThrones().character())
                        .billingProfileType(BillingProfile.Type.COMPANY)
                        .billingProfileId(BillingProfile.Id.random())
                        .adminGithubAvatarUrl(faker.rickAndMorty().character())
                        .build())
                .requestedAt(ZonedDateTime.now())
                .githubUrls(List.of())
                .sponsors(List.of())
                .projectName(faker.rickAndMorty().character())
                .money(MoneyView.builder()
                        .amount(BigDecimal.ONE)
                        .currencyCode(currencyCode)
                        .currencyName(faker.rickAndMorty().location())
                        .build())
                .build();
    }
}
