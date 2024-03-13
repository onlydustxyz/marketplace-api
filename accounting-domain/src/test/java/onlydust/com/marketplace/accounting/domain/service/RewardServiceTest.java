package onlydust.com.marketplace.accounting.domain.service;

import com.github.javafaker.Faker;
import onlydust.com.marketplace.accounting.domain.model.Currency;
import onlydust.com.marketplace.accounting.domain.model.*;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.BillingProfile;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.PayoutInfo;
import onlydust.com.marketplace.accounting.domain.port.out.AccountingRewardStoragePort;
import onlydust.com.marketplace.accounting.domain.port.out.MailNotificationPort;
import onlydust.com.marketplace.accounting.domain.stubs.ERC20Tokens;
import onlydust.com.marketplace.accounting.domain.view.*;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import onlydust.com.marketplace.kernel.model.RewardStatus;
import onlydust.com.marketplace.kernel.model.bank.BankAccount;
import onlydust.com.marketplace.kernel.model.blockchain.evm.EvmAccountAddress;
import onlydust.com.marketplace.kernel.model.blockchain.evm.ethereum.Name;
import onlydust.com.marketplace.kernel.model.blockchain.evm.ethereum.WalletLocator;
import onlydust.com.marketplace.kernel.model.blockchain.starknet.StarknetAccountAddress;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.*;

import static onlydust.com.marketplace.accounting.domain.stubs.Currencies.ETH;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

public class RewardServiceTest {

    private final Faker faker = new Faker();
    private final AccountingRewardStoragePort accountingRewardStoragePort = mock(AccountingRewardStoragePort.class);
    private final AccountingService accountingService = mock(AccountingService.class);
    private final MailNotificationPort mailNotificationPort = mock(MailNotificationPort.class);
    private final RewardService rewardService = new RewardService(accountingRewardStoragePort, accountingService, mailNotificationPort);

    List<Invoice.Id> invoiceIds;
    List<RewardId> rewardIds;
    Set<RewardId> rewardIdsSet;
    List<RewardWithPayoutInfoView> rewardWithPayoutInfoViewList;
    PayoutInfo payoutInfo1;
    PayoutInfo payoutInfo2;
    Currency USDC;
    Currency STRK;

    @BeforeEach
    void setUp() {
        USDC = Currency.of(ERC20Tokens.ETH_USDC).withERC20(ERC20Tokens.OP_USDC);
        STRK = Currency.of(ERC20Tokens.STRK).withERC20(ERC20Tokens.STARKNET_STRK);
        invoiceIds = List.of(Invoice.Id.random(), Invoice.Id.random(), Invoice.Id.random());
        rewardIds = List.of(RewardId.random(), RewardId.random(), RewardId.random(), RewardId.random(), RewardId.random(), RewardId.random());

        payoutInfo1 = PayoutInfo.builder()
                .ethWallet(new WalletLocator(new Name("vitalik.eth")))
                .bankAccount(new BankAccount("PLOP", "9988776655"))
                .starknetAddress(new StarknetAccountAddress("0x1234"))
                .optimismAddress(new EvmAccountAddress("0x111"))
                .build();
        payoutInfo2 = PayoutInfo.builder()
                .ethWallet(new WalletLocator(new Name("foo.eth")))
                .bankAccount(new BankAccount("PLOP", "aabbccddee"))
                .starknetAddress(new StarknetAccountAddress("0x666"))
                .optimismAddress(new EvmAccountAddress("0x222"))
                .build();
        rewardWithPayoutInfoViewList = List.of(
                new RewardWithPayoutInfoView(rewardIds.get(0), payoutInfo1, BigDecimal.valueOf(2)),
                new RewardWithPayoutInfoView(rewardIds.get(1), payoutInfo2, BigDecimal.valueOf(3)),
                new RewardWithPayoutInfoView(rewardIds.get(2), payoutInfo1, BigDecimal.valueOf(4)),
                new RewardWithPayoutInfoView(rewardIds.get(3), payoutInfo2, BigDecimal.valueOf(5)),
                new RewardWithPayoutInfoView(rewardIds.get(4), payoutInfo1, BigDecimal.valueOf(6)),
                new RewardWithPayoutInfoView(rewardIds.get(5), payoutInfo2, BigDecimal.valueOf(7))
        );
        rewardIdsSet = new HashSet<>(rewardIds);
        reset(accountingRewardStoragePort, accountingService, mailNotificationPort);
    }

    @Test
    void should_not_generate_any_batch_given_no_payable_reward() {
        // Given
        when(accountingRewardStoragePort.getRewardWithPayoutInfoOfInvoices(invoiceIds)).thenReturn(rewardWithPayoutInfoViewList);
        when(accountingService.getPayableRewards(rewardIdsSet)).thenReturn(List.of());

        // When
        final var batches = rewardService.createBatchPaymentsForInvoices(invoiceIds);

        // Then
        assertThat(batches).isEmpty();
        verify(accountingRewardStoragePort, never()).saveBatchPayment(any());
    }

    @Test
    void should_generate_batch_given_payable_rewards() {
        // Given
        when(accountingRewardStoragePort.getRewardWithPayoutInfoOfInvoices(invoiceIds)).thenReturn(rewardWithPayoutInfoViewList);
        when(accountingService.getPayableRewards(rewardIdsSet)).thenReturn(List.of(
                new PayableReward(rewardIds.get(0), USDC.forNetwork(Network.ETHEREUM), PositiveAmount.of(100L)),
                new PayableReward(rewardIds.get(1), USDC.forNetwork(Network.OPTIMISM), PositiveAmount.of(200L)),
                new PayableReward(rewardIds.get(2), STRK.forNetwork(Network.ETHEREUM), PositiveAmount.of(300L)),
                new PayableReward(rewardIds.get(3), STRK.forNetwork(Network.STARKNET), PositiveAmount.of(400L)),
                new PayableReward(rewardIds.get(4), ETH.forNetwork(Network.ETHEREUM), PositiveAmount.of(500L)),
                new PayableReward(rewardIds.get(5), USDC.forNetwork(Network.ETHEREUM), PositiveAmount.of(600L))
        ));

        // When
        final var batches = rewardService.createBatchPaymentsForInvoices(invoiceIds);

        // Then
        assertThat(batches).hasSize(3);
        assertThat(batches).extracting(BatchPayment::network).containsExactlyInAnyOrder(Network.ETHEREUM, Network.OPTIMISM, Network.STARKNET);
        {
            final var ethereumBatch = batches.stream().filter(batch -> batch.network().equals(Network.ETHEREUM)).findFirst().orElseThrow();
            assertThat(ethereumBatch.rewardIds()).containsExactlyInAnyOrder(rewardIds.get(0), rewardIds.get(2), rewardIds.get(4), rewardIds.get(5));
            assertThat(ethereumBatch.moneys()).extracting(MoneyView::currencyCode).containsExactlyInAnyOrder(Currency.Code.USDC_STR, Currency.Code.STRK_STR,
                    Currency.Code.ETH_STR);
            assertThat(getMoneyView(ethereumBatch, Currency.Code.USDC_STR).amount()).isEqualTo(BigDecimal.valueOf(700L));
            assertThat(getMoneyView(ethereumBatch, Currency.Code.USDC_STR).dollarsEquivalent()).isEqualTo(BigDecimal.valueOf(4400L));
            assertThat(getMoneyView(ethereumBatch, Currency.Code.STRK_STR).amount()).isEqualTo(BigDecimal.valueOf(300L));
            assertThat(getMoneyView(ethereumBatch, Currency.Code.STRK_STR).dollarsEquivalent()).isEqualTo(BigDecimal.valueOf(1200L));
            assertThat(getMoneyView(ethereumBatch, Currency.Code.ETH_STR).amount()).isEqualTo(BigDecimal.valueOf(500L));
            assertThat(getMoneyView(ethereumBatch, Currency.Code.ETH_STR).dollarsEquivalent()).isEqualTo(BigDecimal.valueOf(3000L));
            assertThat(ethereumBatch.csv()).isEqualToIgnoringWhitespace("""
                    erc20,0xa0b86991c6218b36c1d19d4a2e9eb0ce3606eb48,vitalik.eth,100
                    erc20,0xCa14007Eff0dB1f8135f4C25B34De49AB0d42766,vitalik.eth,300
                    native,,vitalik.eth,500
                    erc20,0xa0b86991c6218b36c1d19d4a2e9eb0ce3606eb48,foo.eth,600
                    """);
            verify(accountingRewardStoragePort).saveBatchPayment(ethereumBatch);
        }
        {
            final var optimismBatch = batches.stream().filter(batch -> batch.network().equals(Network.OPTIMISM)).findFirst().orElseThrow();
            assertThat(optimismBatch.rewardIds()).containsExactlyInAnyOrder(rewardIds.get(1));
            assertThat(optimismBatch.moneys()).extracting(MoneyView::currencyCode).containsExactlyInAnyOrder(Currency.Code.USDC_STR);
            assertThat(getMoneyView(optimismBatch, Currency.Code.USDC_STR).amount()).isEqualTo(BigDecimal.valueOf(200L));
            assertThat(optimismBatch.csv()).isEqualToIgnoringWhitespace("""
                    erc20,0x0b2C639c533813f4Aa9D7837CAf62653d097Ff85,0x0222,200
                    """);
            verify(accountingRewardStoragePort).saveBatchPayment(optimismBatch);
        }
        {
            final var starknetBatch = batches.stream().filter(batch -> batch.network().equals(Network.STARKNET)).findFirst().orElseThrow();
            assertThat(starknetBatch.rewardIds()).containsExactlyInAnyOrder(rewardIds.get(3));
            assertThat(starknetBatch.moneys()).extracting(MoneyView::currencyCode).containsExactlyInAnyOrder(Currency.Code.STRK_STR);
            assertThat(getMoneyView(starknetBatch, Currency.Code.STRK_STR).amount()).isEqualTo(BigDecimal.valueOf(400L));
            assertThat(starknetBatch.csv()).isEqualToIgnoringWhitespace("""
                    erc20,0xCa14007Eff0dB1f8135f4C25B34De49AB0d42766,0x0666,400
                    """);
            verify(accountingRewardStoragePort).saveBatchPayment(starknetBatch);
        }

    }

    @NotNull
    private static MoneyView getMoneyView(BatchPayment batchPayment, String currencyCode) {
        return batchPayment.moneys().stream().filter(moneyView -> moneyView.currencyCode().equals(currencyCode)).findFirst().orElseThrow();
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
                        .network(Network.STARKNET)
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
                .network(Network.STARKNET)
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
        //TODO
        //verify(oldRewardStoragePort).markRewardAsPaid(payableRewardWithPayoutInfoViews.get(0), transactionHash);
        //verify(oldRewardStoragePort).markRewardAsPaid(payableRewardWithPayoutInfoViews.get(1), transactionHash);
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
                        generateRewardStubForCurrency(Currency.Code.USD_STR),
                        generateRewardStubForCurrency(Currency.Code.USDC_STR),
                        generateRewardStubForCurrency(Currency.Code.APT_STR),
                        generateRewardStubForCurrency(Currency.Code.LORDS_STR),
                        generateRewardStubForCurrency(Currency.Code.STRK_STR),
                        BackofficeRewardView.builder()
                                .id(RewardId.random())
                                .status(RewardStatus.PROCESSING)
                                .billingProfileAdmin(ShortBillingProfileAdminView.builder()
                                        .admins(List.of(
                                                new ShortBillingProfileAdminView.Admin(faker.name().username(),
                                                        faker.internet().avatar(),
                                                        faker.internet().emailAddress(),
                                                        faker.name().firstName(),
                                                        faker.name().lastName())
                                        ))
                                        .billingProfileName(faker.gameOfThrones().character())
                                        .billingProfileType(BillingProfile.Type.COMPANY)
                                        .billingProfileId(BillingProfile.Id.random())
                                        .build())
                                .requestedAt(ZonedDateTime.now())
                                .githubUrls(List.of())
                                .sponsors(List.of())
                                .project(new ShortProjectView(ProjectId.random(), faker.rickAndMorty().character(), faker.internet().url(),
                                        faker.weather().description(), faker.name().username()))
                                .processedAt(ZonedDateTime.now())
                                .money(MoneyView.builder()
                                        .amount(BigDecimal.ONE)
                                        .currencyCode(Currency.Code.USDC_STR)
                                        .currencyName(faker.rickAndMorty().location())
                                        .build())
                                .transactionReferences(List.of(faker.random().hex()))
                                .paidToAccountNumbers(List.of(faker.random().hex()))
                                .build(),
                        BackofficeRewardView.builder()
                                .id(RewardId.random())
                                .status(RewardStatus.PROCESSING)
                                .billingProfileAdmin(ShortBillingProfileAdminView.builder()
                                        .admins(List.of(
                                                new ShortBillingProfileAdminView.Admin(faker.name().username(),
                                                        faker.internet().avatar(),
                                                        faker.internet().emailAddress(),
                                                        faker.name().firstName(),
                                                        faker.name().lastName())
                                        ))
                                        .billingProfileName(faker.gameOfThrones().character())
                                        .billingProfileType(BillingProfile.Type.COMPANY)
                                        .billingProfileId(BillingProfile.Id.random())
                                        .build())
                                .requestedAt(ZonedDateTime.now())
                                .githubUrls(List.of())
                                .sponsors(List.of())
                                .project(new ShortProjectView(ProjectId.random(), faker.rickAndMorty().character(), faker.internet().url(),
                                        faker.weather().description(), faker.name().username()))
                                .transactionReferences(List.of(faker.random().hex()))
                                .paidToAccountNumbers(List.of(faker.random().hex()))
                                .money(MoneyView.builder()
                                        .amount(BigDecimal.ONE)
                                        .currencyCode(Currency.Code.USDC_STR)
                                        .currencyName(faker.rickAndMorty().location())
                                        .build())
                                .build()
                ));
        final List<BackofficeRewardView> rewardViews = rewardService.searchForBatchPaymentByInvoiceIds(invoiceIds);

        // Then
        assertEquals(3, rewardViews.size());
        assertEquals(Currency.Code.USDC_STR, rewardViews.get(0).money().currencyCode());
        assertEquals(Currency.Code.LORDS_STR, rewardViews.get(1).money().currencyCode());
        assertEquals(Currency.Code.STRK_STR, rewardViews.get(2).money().currencyCode());
    }

    @Test
    void should_notify_new_rewards_were_paid() {
        // Given
        final String email1 = faker.rickAndMorty().character();
        final String email2 = faker.gameOfThrones().character();
        final var r11 = generateRewardStubForCurrencyAndEmail("USD", email1);
        final var r21 = generateRewardStubForCurrencyAndEmail("STRK", email2);
        final var r12 = generateRewardStubForCurrencyAndEmail("OP", email1);
        final var r22 = generateRewardStubForCurrencyAndEmail("APT", email2);
        final List<BackofficeRewardView> rewardViews = List.of(
                r11,
                r12,
                r21,
                r22
        );

        // When
        when(accountingRewardStoragePort.findPaidRewardsToNotify())
                .thenReturn(rewardViews);
        rewardService.notifyAllNewPaidRewards();

        // Then
        verify(mailNotificationPort, times(1)).sendRewardsPaidMail(email1, List.of(r11, r12));
        verify(mailNotificationPort, times(1)).sendRewardsPaidMail(email2, List.of(r21, r22));
        verify(accountingRewardStoragePort).markRewardsAsPaymentNotified(rewardViews.stream().map(BackofficeRewardView::id).toList());
    }

    private BackofficeRewardView generateRewardStubForCurrency(final String currencyCode) {
        return generateRewardStubForCurrencyAndEmail(currencyCode, faker.rickAndMorty().character());
    }

    private BackofficeRewardView generateRewardStubForCurrencyAndEmail(final String currencyCode, final String email) {
        return BackofficeRewardView.builder()
                .id(RewardId.random())
                .status(RewardStatus.PROCESSING)
                .billingProfileAdmin(ShortBillingProfileAdminView.builder()
                        .admins(List.of(
                                new ShortBillingProfileAdminView.Admin(faker.name().username(),
                                        faker.internet().avatar(),
                                        email,
                                        faker.name().firstName(),
                                        faker.name().lastName())
                        ))
                        .billingProfileName(faker.gameOfThrones().character())
                        .billingProfileType(BillingProfile.Type.COMPANY)
                        .billingProfileId(BillingProfile.Id.random())
                        .build())
                .requestedAt(ZonedDateTime.now())
                .githubUrls(List.of())
                .sponsors(List.of())
                .project(new ShortProjectView(ProjectId.random(), faker.rickAndMorty().character(), faker.internet().url(), faker.weather().description(),
                        faker.name().username()))
                .money(MoneyView.builder()
                        .amount(BigDecimal.ONE)
                        .currencyCode(currencyCode)
                        .currencyName(faker.rickAndMorty().location())
                        .build())
                .transactionReferences(List.of())
                .paidToAccountNumbers(List.of())
                .build();
    }
}
