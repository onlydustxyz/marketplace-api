package onlydust.com.marketplace.accounting.domain.service;

import com.github.javafaker.Faker;
import onlydust.com.marketplace.accounting.domain.model.*;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.BillingProfile;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.PayoutInfo;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.VerificationStatus;
import onlydust.com.marketplace.accounting.domain.model.user.UserId;
import onlydust.com.marketplace.accounting.domain.port.out.AccountingRewardStoragePort;
import onlydust.com.marketplace.accounting.domain.port.out.InvoiceStoragePort;
import onlydust.com.marketplace.accounting.domain.stubs.ERC20Tokens;
import onlydust.com.marketplace.accounting.domain.view.BatchPaymentDetailsView;
import onlydust.com.marketplace.accounting.domain.view.BillingProfileView;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import onlydust.com.marketplace.kernel.model.bank.BankAccount;
import onlydust.com.marketplace.kernel.model.blockchain.evm.EvmAccountAddress;
import onlydust.com.marketplace.kernel.model.blockchain.evm.ethereum.Name;
import onlydust.com.marketplace.kernel.model.blockchain.evm.ethereum.WalletLocator;
import onlydust.com.marketplace.kernel.model.blockchain.starknet.StarknetAccountAddress;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static onlydust.com.marketplace.accounting.domain.stubs.BillingProfileHelper.newKyc;
import static onlydust.com.marketplace.accounting.domain.stubs.Currencies.ETH;
import static onlydust.com.marketplace.accounting.domain.stubs.Currencies.USD;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

public class BatchPaymentServiceTest {

    private final Faker faker = new Faker();
    private final AccountingRewardStoragePort accountingRewardStoragePort = mock(AccountingRewardStoragePort.class);
    private final AccountingService accountingService = mock(AccountingService.class);
    private final InvoiceStoragePort invoiceStoragePort = mock(InvoiceStoragePort.class);
    private final BatchPaymentService rewardService = new BatchPaymentService(accountingRewardStoragePort, invoiceStoragePort, accountingService);

    List<Invoice.Id> invoiceIds;
    List<Invoice> invoices;
    List<RewardId> rewardIds;
    Set<RewardId> rewardIdsSet;
    List<PayableReward> payableRewards;
    PayoutInfo payoutInfo1;
    PayoutInfo payoutInfo2;
    Currency USDC;
    Currency STRK;
    BillingProfileView billingProfile1;
    BillingProfileView billingProfile2;

    @BeforeEach
    void setUp() {
        USDC = Currency.of(ERC20Tokens.ETH_USDC).withERC20(ERC20Tokens.OP_USDC);
        STRK = Currency.of(ERC20Tokens.STRK).withERC20(ERC20Tokens.STARKNET_STRK);
        invoiceIds = List.of(Invoice.Id.random(), Invoice.Id.random(), Invoice.Id.random());
        rewardIds = List.of(RewardId.random(), RewardId.random(), RewardId.random(), RewardId.random(), RewardId.random(), RewardId.random());

        payoutInfo1 = PayoutInfo.builder()
                .ethWallet(new WalletLocator(new Name("vitalik.eth")))
                .build();
        payoutInfo2 = PayoutInfo.builder()
                .ethWallet(new WalletLocator(new Name("foo.eth")))
                .bankAccount(new BankAccount("PLOP", "aabbccddee"))
                .starknetAddress(new StarknetAccountAddress("0x666"))
                .optimismAddress(new EvmAccountAddress("0x222"))
                .build();

        final var billingProfileId1 = BillingProfile.Id.random();
        billingProfile1 = BillingProfileView.builder()
                .id(billingProfileId1)
                .type(BillingProfile.Type.INDIVIDUAL)
                .payoutInfo(payoutInfo1)
                .verificationStatus(VerificationStatus.VERIFIED)
                .name("John")
                .kyc(newKyc(billingProfileId1, UserId.random()))
                .build();
        final var billingProfileId2 = BillingProfile.Id.random();
        billingProfile2 = BillingProfileView.builder()
                .id(billingProfileId2)
                .type(BillingProfile.Type.INDIVIDUAL)
                .payoutInfo(payoutInfo2)
                .verificationStatus(VerificationStatus.VERIFIED)
                .name("John")
                .kyc(newKyc(billingProfileId2, UserId.random()))
                .build();

        invoices = List.of(
                Invoice.of(billingProfile1, 1, UserId.random())
                        .rewards(List.of(
                                new Invoice.Reward(rewardIds.get(0), ZonedDateTime.now().minusDays(1), faker.lordOfTheRings().location(),
                                        Money.of(0L, ETH), Money.of(0L, USD), null, List.of()),
                                new Invoice.Reward(rewardIds.get(2), ZonedDateTime.now().minusDays(1), faker.lordOfTheRings().location(),
                                        Money.of(0L, ETH), Money.of(0L, USD), null, List.of()),
                                new Invoice.Reward(rewardIds.get(4), ZonedDateTime.now().minusDays(1), faker.lordOfTheRings().location(),
                                        Money.of(0L, ETH), Money.of(0L, USD), null, List.of())
                        )),
                Invoice.of(billingProfile2, 1, UserId.random())
                        .rewards(List.of(
                                new Invoice.Reward(rewardIds.get(1), ZonedDateTime.now().minusDays(1), faker.lordOfTheRings().location(),
                                        Money.of(0L, ETH), Money.of(0L, USD), null, List.of()),
                                new Invoice.Reward(rewardIds.get(3), ZonedDateTime.now().minusDays(1), faker.lordOfTheRings().location(),
                                        Money.of(0L, ETH), Money.of(0L, USD), null, List.of()),
                                new Invoice.Reward(rewardIds.get(5), ZonedDateTime.now().minusDays(1), faker.lordOfTheRings().location(),
                                        Money.of(0L, ETH), Money.of(0L, USD), null, List.of())
                        ))
        );
        payableRewards = List.of(
                new PayableReward(rewardIds.get(0), USDC.forNetwork(Network.ETHEREUM), PositiveAmount.of(100L)),
                new PayableReward(rewardIds.get(1), USDC.forNetwork(Network.OPTIMISM), PositiveAmount.of(200L)),
                new PayableReward(rewardIds.get(2), STRK.forNetwork(Network.ETHEREUM), PositiveAmount.of(300L)),
                new PayableReward(rewardIds.get(3), STRK.forNetwork(Network.STARKNET), PositiveAmount.of(400L)),
                new PayableReward(rewardIds.get(4), ETH.forNetwork(Network.ETHEREUM), PositiveAmount.of(500L)),
                new PayableReward(rewardIds.get(5), USDC.forNetwork(Network.ETHEREUM), PositiveAmount.of(600L))
        );
        rewardIdsSet = new HashSet<>(rewardIds);
        reset(accountingRewardStoragePort, invoiceStoragePort, accountingService);
    }

    @Test
    void should_not_generate_any_batch_given_no_payable_reward() {
        // Given
        when(invoiceStoragePort.getAll(invoiceIds)).thenReturn(invoices);
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
        when(invoiceStoragePort.getAll(invoiceIds)).thenReturn(invoices);
        when(accountingService.getPayableRewards(rewardIdsSet)).thenReturn(payableRewards);

        // When
        final var batches = rewardService.createBatchPaymentsForInvoices(invoiceIds).stream().map(BatchPaymentDetailsView::batchPayment).toList();

        // Then
        assertThat(batches).hasSize(3);
        assertThat(batches).extracting(BatchPayment::network).containsExactlyInAnyOrder(Network.ETHEREUM, Network.OPTIMISM, Network.STARKNET);
        {
            final var ethereumBatch = batches.stream().filter(batch -> batch.network().equals(Network.ETHEREUM)).findFirst().orElseThrow();
            assertThat(ethereumBatch.rewards()).containsExactlyInAnyOrder(payableRewards.get(0), payableRewards.get(2), payableRewards.get(4),
                    payableRewards.get(5));
            assertThat(ethereumBatch.invoices()).containsAll(invoices);
            assertThat(ethereumBatch.csv()).hasLineCount(4);
            assertThat(ethereumBatch.csv()).contains("erc20,0xa0b86991c6218b36c1d19d4a2e9eb0ce3606eb48,vitalik.eth,100");
            assertThat(ethereumBatch.csv()).contains("erc20,0xCa14007Eff0dB1f8135f4C25B34De49AB0d42766,vitalik.eth,300");
            assertThat(ethereumBatch.csv()).contains("native,,vitalik.eth,500");
            assertThat(ethereumBatch.csv()).contains("erc20,0xa0b86991c6218b36c1d19d4a2e9eb0ce3606eb48,foo.eth,600");
        }
        {
            final var optimismBatch = batches.stream().filter(batch -> batch.network().equals(Network.OPTIMISM)).findFirst().orElseThrow();
            assertThat(optimismBatch.rewards()).containsExactlyInAnyOrder(payableRewards.get(1));
            assertThat(optimismBatch.invoices()).containsAll(invoices);
            assertThat(optimismBatch.csv()).isEqualToIgnoringWhitespace("""
                    erc20,0x0b2C639c533813f4Aa9D7837CAf62653d097Ff85,0x0222,200
                    """);
        }
        {
            final var starknetBatch = batches.stream().filter(batch -> batch.network().equals(Network.STARKNET)).findFirst().orElseThrow();
            assertThat(starknetBatch.rewards()).containsExactlyInAnyOrder(payableRewards.get(3));
            assertThat(starknetBatch.invoices()).containsAll(invoices);
            assertThat(starknetBatch.csv()).isEqualToIgnoringWhitespace("""
                    erc20,0xCa14007Eff0dB1f8135f4C25B34De49AB0d42766,0x0666,400
                    """);
        }
        final var savedBatches = ArgumentCaptor.forClass(List.class);
        verify(accountingRewardStoragePort).saveAll(savedBatches.capture());
        assertThat(savedBatches.getValue()).containsAll(batches);
    }


    @Test
    void should_raise_not_found_exception_given_not_existing_batch_payment() {
        // Given
        final BatchPayment.Id batchPaymentId = BatchPayment.Id.random();

        // When
        when(accountingRewardStoragePort.findBatchPayment(batchPaymentId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> rewardService.markBatchPaymentAsPaid(batchPaymentId, faker.random().hex()))
                .isInstanceOf(OnlyDustException.class)
                .hasMessage("Batch payment %s not found".formatted(batchPaymentId.value()));
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
                        .network(Network.STARKNET)
                        .rewards(List.of())
                        .invoices(List.of())
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
        assertEquals("Provided hash is not hexadecimal", exception.getMessage());
    }

    @Test
    void should_update_batch_payment_and_linked_rewards_with_transaction_hash() {
        // Given
        final BatchPayment.Id batchPaymentId = BatchPayment.Id.random();
        final String transactionHash = "0x" + faker.random().hex();
        final BatchPayment batchPayment = BatchPayment.builder()
                .id(batchPaymentId)
                .network(Network.ETHEREUM)
                .csv(faker.gameOfThrones().character())
                .rewards(payableRewards.stream().filter(pr -> pr.currency().network().equals(Network.ETHEREUM)).toList())
                .invoices(invoices)
                .build();
        final BatchPayment updatedBatchPayment = batchPayment.toBuilder()
                .status(BatchPayment.Status.PAID)
                .transactionHash(transactionHash)
                .build();


        // When
        when(accountingRewardStoragePort.findBatchPayment(batchPaymentId)).thenReturn(Optional.of(batchPayment));
        rewardService.markBatchPaymentAsPaid(batchPaymentId, transactionHash);

        // Then
        verify(accountingRewardStoragePort).saveBatchPayment(updatedBatchPayment);
        verify(accountingService).pay(payableRewards.get(0).id(),
                payableRewards.get(0).currency().id(),
                new SponsorAccount.PaymentReference(Network.ETHEREUM, transactionHash, invoices.get(0).billingProfileSnapshot().subject(), "vitalik.eth"));

        verify(accountingService).pay(payableRewards.get(2).id(),
                payableRewards.get(2).currency().id(),
                new SponsorAccount.PaymentReference(Network.ETHEREUM, transactionHash, invoices.get(0).billingProfileSnapshot().subject(), "vitalik.eth"));

        verify(accountingService).pay(payableRewards.get(4).id(),
                payableRewards.get(4).currency().id(),
                new SponsorAccount.PaymentReference(Network.ETHEREUM, transactionHash, invoices.get(0).billingProfileSnapshot().subject(), "vitalik.eth"));

        verify(accountingService).pay(payableRewards.get(5).id(),
                payableRewards.get(5).currency().id(),
                new SponsorAccount.PaymentReference(Network.ETHEREUM, transactionHash, invoices.get(1).billingProfileSnapshot().subject(), "foo.eth"));
    }
}
