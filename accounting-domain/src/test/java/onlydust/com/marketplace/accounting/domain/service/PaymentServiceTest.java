package onlydust.com.marketplace.accounting.domain.service;

import com.github.javafaker.Faker;
import onlydust.com.marketplace.accounting.domain.model.*;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.*;
import onlydust.com.marketplace.accounting.domain.port.in.BlockchainFacadePort;
import onlydust.com.marketplace.accounting.domain.port.out.AccountingRewardStoragePort;
import onlydust.com.marketplace.accounting.domain.port.out.BlockchainTransactionStoragePort;
import onlydust.com.marketplace.accounting.domain.port.out.InvoiceStoragePort;
import onlydust.com.marketplace.accounting.domain.stubs.ERC20Tokens;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import onlydust.com.marketplace.kernel.model.RewardId;
import onlydust.com.marketplace.kernel.model.UserId;
import onlydust.com.marketplace.kernel.model.bank.BankAccount;
import onlydust.com.marketplace.kernel.model.blockchain.Ethereum;
import onlydust.com.marketplace.kernel.model.blockchain.evm.EvmAccountAddress;
import onlydust.com.marketplace.kernel.model.blockchain.evm.EvmTransaction;
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

import static onlydust.com.marketplace.accounting.domain.stubs.BillingProfileHelper.newKyb;
import static onlydust.com.marketplace.accounting.domain.stubs.BillingProfileHelper.newKyc;
import static onlydust.com.marketplace.accounting.domain.stubs.Currencies.ETH;
import static onlydust.com.marketplace.accounting.domain.stubs.Currencies.USD;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

public class PaymentServiceTest {

    private final Faker faker = new Faker();
    private final AccountingRewardStoragePort accountingRewardStoragePort = mock(AccountingRewardStoragePort.class);
    private final AccountingService accountingService = mock(AccountingService.class);
    private final InvoiceStoragePort invoiceStoragePort = mock(InvoiceStoragePort.class);
    private final BlockchainTransactionStoragePort ethereumTransactionStoragePort = mock(BlockchainTransactionStoragePort.class);
    private final BlockchainFacadePort blockchainFacadePort = new BlockchainService(
            ethereumTransactionStoragePort,
            mock(BlockchainTransactionStoragePort.class),
            mock(BlockchainTransactionStoragePort.class),
            mock(BlockchainTransactionStoragePort.class),
            mock(BlockchainTransactionStoragePort.class));
    private final PaymentService rewardService = new PaymentService(accountingRewardStoragePort, invoiceStoragePort, accountingService, blockchainFacadePort);

    List<Invoice.Id> invoiceIds;
    List<Invoice> invoices;
    List<RewardId> rewardIds;
    Set<RewardId> rewardIdsSet;
    List<PayableReward> payableRewards;
    PayoutInfo payoutInfo1;
    PayoutInfo payoutInfo2;
    Currency USDC;
    Currency STRK;
    IndividualBillingProfile billingProfile1;
    CompanyBillingProfile billingProfile2;

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
        billingProfile1 = IndividualBillingProfile.builder()
                .id(billingProfileId1)
                .status(VerificationStatus.VERIFIED)
                .name("John")
                .kyc(newKyc(billingProfileId1, UserId.random()))
                .enabled(true)
                .owner(new BillingProfile.User(UserId.random(), BillingProfile.User.Role.ADMIN, ZonedDateTime.now()))
                .build();
        final var billingProfileId2 = BillingProfile.Id.random();
        billingProfile2 = CompanyBillingProfile.builder()
                .id(billingProfileId2)
                .status(VerificationStatus.VERIFIED)
                .name("John")
                .kyb(newKyb(billingProfileId2, UserId.random()))
                .enabled(true)
                .members(Set.of(new BillingProfile.User(UserId.random(), BillingProfile.User.Role.ADMIN, ZonedDateTime.now())))
                .build();

        invoices = List.of(
                Invoice.of(billingProfile1, 1, UserId.random(), payoutInfo1)
                        .rewards(List.of(
                                new Invoice.Reward(rewardIds.get(0), ZonedDateTime.now().minusDays(1), faker.lordOfTheRings().location(),
                                        Money.of(0L, ETH), Money.of(0L, USD), null, List.of()),
                                new Invoice.Reward(rewardIds.get(2), ZonedDateTime.now().minusDays(1), faker.lordOfTheRings().location(),
                                        Money.of(0L, ETH), Money.of(0L, USD), null, List.of()),
                                new Invoice.Reward(rewardIds.get(4), ZonedDateTime.now().minusDays(1), faker.lordOfTheRings().location(),
                                        Money.of(0L, ETH), Money.of(0L, USD), null, List.of())
                        )),
                Invoice.of(billingProfile2, 1, UserId.random(), payoutInfo2)
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
                PayableReward.of(rewardIds.get(0), USDC.forNetwork(Network.ETHEREUM), PositiveAmount.of(100L), invoices.get(0).billingProfileSnapshot()),
                PayableReward.of(rewardIds.get(1), USDC.forNetwork(Network.OPTIMISM), PositiveAmount.of(200L), invoices.get(1).billingProfileSnapshot()),
                PayableReward.of(rewardIds.get(2), STRK.forNetwork(Network.ETHEREUM), PositiveAmount.of(300L), invoices.get(0).billingProfileSnapshot()),
                PayableReward.of(rewardIds.get(3), STRK.forNetwork(Network.STARKNET), PositiveAmount.of(400L), invoices.get(1).billingProfileSnapshot()),
                PayableReward.of(rewardIds.get(4), ETH.forNetwork(Network.ETHEREUM), PositiveAmount.of(500L), invoices.get(0).billingProfileSnapshot()),
                PayableReward.of(rewardIds.get(5), USDC.forNetwork(Network.ETHEREUM), PositiveAmount.of(600L), invoices.get(1).billingProfileSnapshot())
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
        final var batches = rewardService.createPaymentsForInvoices(invoiceIds);

        // Then
        assertThat(batches).isEmpty();
        verify(accountingRewardStoragePort, never()).savePayment(any());
    }

    @Test
    void should_generate_batch_given_payable_rewards() {
        // Given
        when(invoiceStoragePort.getAll(invoiceIds)).thenReturn(invoices);
        when(accountingService.pay(rewardIdsSet)).thenReturn(List.of(
                Payment.of(Network.ETHEREUM, payableRewards.stream().filter(pr -> pr.currency().network().equals(Network.ETHEREUM)).toList()),
                Payment.of(Network.OPTIMISM, payableRewards.stream().filter(pr -> pr.currency().network().equals(Network.OPTIMISM)).toList()),
                Payment.of(Network.STARKNET, payableRewards.stream().filter(pr -> pr.currency().network().equals(Network.STARKNET)).toList())
        ));

        // When
        final var batches = rewardService.createPaymentsForInvoices(invoiceIds);

        // Then
        assertThat(batches).hasSize(3);
        assertThat(batches).extracting(Payment::network).containsExactlyInAnyOrder(Network.ETHEREUM, Network.OPTIMISM, Network.STARKNET);
        {
            final var ethereumBatch = batches.stream().filter(batch -> batch.network().equals(Network.ETHEREUM)).findFirst().orElseThrow();
            assertThat(ethereumBatch.rewards()).containsExactlyInAnyOrder(payableRewards.get(0), payableRewards.get(2), payableRewards.get(4),
                    payableRewards.get(5));
            assertThat(ethereumBatch.csv()).hasLineCount(4);
            assertThat(ethereumBatch.csv()).contains("erc20,0xa0b86991c6218b36c1d19d4a2e9eb0ce3606eb48,vitalik.eth,100,");
            assertThat(ethereumBatch.csv()).contains("erc20,0xCa14007Eff0dB1f8135f4C25B34De49AB0d42766,vitalik.eth,300,");
            assertThat(ethereumBatch.csv()).contains("native,,vitalik.eth,500,");
            assertThat(ethereumBatch.csv()).contains("erc20,0xa0b86991c6218b36c1d19d4a2e9eb0ce3606eb48,foo.eth,720.0,");
        }
        {
            final var optimismBatch = batches.stream().filter(batch -> batch.network().equals(Network.OPTIMISM)).findFirst().orElseThrow();
            assertThat(optimismBatch.rewards()).containsExactlyInAnyOrder(payableRewards.get(1));
            assertThat(optimismBatch.csv()).isEqualToIgnoringWhitespace("""
                    erc20,0x0b2C639c533813f4Aa9D7837CAf62653d097Ff85,0x0222,240.0,
                    """);
        }
        {
            final var starknetBatch = batches.stream().filter(batch -> batch.network().equals(Network.STARKNET)).findFirst().orElseThrow();
            assertThat(starknetBatch.rewards()).containsExactlyInAnyOrder(payableRewards.get(3));
            assertThat(starknetBatch.csv()).isEqualToIgnoringWhitespace("""
                    erc20,0xCa14007Eff0dB1f8135f4C25B34De49AB0d42766,0x0666,480.0,
                    """);
        }
        final var savedBatches = ArgumentCaptor.forClass(List.class);
        verify(accountingRewardStoragePort).saveAll(savedBatches.capture());
        assertThat(savedBatches.getValue()).containsAll(batches);
    }

    @Test
    void should_raise_not_found_exception_given_not_existing_batch_payment() {
        // Given
        final Payment.Id batchPaymentId = Payment.Id.random();

        // When
        when(accountingRewardStoragePort.findPayment(batchPaymentId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> rewardService.markPaymentAsPaid(batchPaymentId, faker.random().hex()))
                .isInstanceOf(OnlyDustException.class)
                .hasMessage("Batch payment %s not found".formatted(batchPaymentId.value()));
    }


    @Test
    void should_raise_wrong_transaction_hash_exception() {
        // Given
        final Payment.Id batchPaymentId = Payment.Id.random();
        final String transactionHash = faker.rickAndMorty().character();

        // When
        when(accountingRewardStoragePort.findPayment(batchPaymentId))
                .thenReturn(Optional.of(Payment.builder()
                        .csv("")
                        .id(Payment.Id.random())
                        .network(Network.STARKNET)
                        .rewards(List.of())
                        .build()));
        Exception exception = null;
        try {
            rewardService.markPaymentAsPaid(batchPaymentId, transactionHash);
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
        final Payment.Id batchPaymentId = Payment.Id.random();
        final String transactionHash = "0x" + faker.random().hex();
        final Payment payment = Payment.builder()
                .id(batchPaymentId)
                .network(Network.ETHEREUM)
                .csv(faker.gameOfThrones().character())
                .rewards(payableRewards.stream().filter(pr -> pr.currency().network().equals(Network.ETHEREUM)).toList())
                .build();
        final Payment updatedPayment = payment.toBuilder()
                .status(Payment.Status.PAID)
                .transactionHash(transactionHash)
                .build();

        // When
        when(accountingRewardStoragePort.findPayment(batchPaymentId)).thenReturn(Optional.of(payment));
        when(ethereumTransactionStoragePort.get(Ethereum.transactionHash(transactionHash)))
                .thenReturn(Optional.of(new EvmTransaction(Ethereum.transactionHash(transactionHash), ZonedDateTime.now())));
        rewardService.markPaymentAsPaid(batchPaymentId, transactionHash);

        // Then
        verify(accountingRewardStoragePort).savePayment(updatedPayment);
        verify(accountingService).confirm(updatedPayment);
    }
}
