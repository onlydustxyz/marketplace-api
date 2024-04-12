package onlydust.com.marketplace.accounting.domain;

import com.github.javafaker.Faker;
import onlydust.com.marketplace.accounting.domain.model.*;
import onlydust.com.marketplace.accounting.domain.model.SponsorAccount.Transaction;
import onlydust.com.marketplace.accounting.domain.model.accountbook.AccountBook.AccountId;
import onlydust.com.marketplace.accounting.domain.model.accountbook.AccountBookAggregate.*;
import onlydust.com.marketplace.accounting.domain.model.accountbook.AccountBookObserver;
import onlydust.com.marketplace.accounting.domain.model.accountbook.IdentifiedAccountBookEvent;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.BillingProfile;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.Kyc;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.PayoutInfo;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.VerificationStatus;
import onlydust.com.marketplace.accounting.domain.model.user.UserId;
import onlydust.com.marketplace.accounting.domain.port.out.AccountingObserverPort;
import onlydust.com.marketplace.accounting.domain.port.out.CurrencyStorage;
import onlydust.com.marketplace.accounting.domain.port.out.InvoiceStoragePort;
import onlydust.com.marketplace.accounting.domain.port.out.ProjectAccountingObserver;
import onlydust.com.marketplace.accounting.domain.service.AccountBookFacade;
import onlydust.com.marketplace.accounting.domain.service.AccountingService;
import onlydust.com.marketplace.accounting.domain.service.CachedAccountBookProvider;
import onlydust.com.marketplace.accounting.domain.stubs.AccountBookEventStorageStub;
import onlydust.com.marketplace.accounting.domain.stubs.Currencies;
import onlydust.com.marketplace.accounting.domain.stubs.ERC20Tokens;
import onlydust.com.marketplace.accounting.domain.stubs.SponsorAccountStorageStub;
import onlydust.com.marketplace.accounting.domain.view.BillingProfileView;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

public class AccountingServiceTest {
    final SponsorAccountStorageStub sponsorAccountStorage = new SponsorAccountStorageStub();
    final CurrencyStorage currencyStorage = mock(CurrencyStorage.class);
    final AccountingObserverPort accountingObserver = mock(AccountingObserverPort.class);
    final ProjectAccountingObserver projectAccountingObserver = mock(ProjectAccountingObserver.class);
    final InvoiceStoragePort invoiceStoragePort = mock(InvoiceStoragePort.class);
    AccountBookObserver accountBookObserver = mock(AccountBookObserver.class);
    AccountBookEventStorageStub accountBookEventStorage;
    AccountingService accountingService;
    final Faker faker = new Faker();
    final Invoice invoice = Invoice.of(BillingProfileView.builder()
                    .id(BillingProfile.Id.random())
                    .type(BillingProfile.Type.INDIVIDUAL)
                    .kyc(Kyc.builder().id(UUID.randomUUID()).ownerId(UserId.random()).status(VerificationStatus.VERIFIED).country(Country.fromIso3("FRA")).firstName(faker.name().firstName()).address(faker.address().fullAddress()).usCitizen(false).build())
                    .payoutInfo(PayoutInfo.builder().build())
                    .name("OnlyDust")
                    .build(), 1, UserId.random())
            .status(Invoice.Status.APPROVED)
            .rewards(List.of());

    private Payment.Reference fakePaymentReference(Network network) {
        return new Payment.Reference(ZonedDateTime.now(), network, faker.random().hex(), faker.rickAndMorty().character(), faker.internet().slug() + ".eth");
    }

    private Transaction fakeTransaction(Network network, Amount amount) {
        return amount.isPositive() ?
                new Transaction(Transaction.Type.DEPOSIT, fakePaymentReference(network), PositiveAmount.of(amount)) :
                new Transaction(Transaction.Type.WITHDRAW, fakePaymentReference(network), PositiveAmount.of(amount.negate()));
    }

    private void assertOnRewardCreated(RewardId rewardId, boolean isFunded, ZonedDateTime unlockDate, Set<Network> networks) {
        final var accountBookFacadeCaptor = ArgumentCaptor.forClass(AccountBookFacade.class);
        verify(accountingObserver).onRewardCreated(eq(rewardId), accountBookFacadeCaptor.capture());
        final var accountBookFacade = accountBookFacadeCaptor.getValue();
        assertThat(accountBookFacade.isFunded(rewardId)).isEqualTo(isFunded);
        if (unlockDate == null)
            assertThat(accountBookFacade.unlockDateOf(rewardId)).isEmpty();
        else {
            assertThat(accountBookFacade.unlockDateOf(rewardId)).isPresent();
            assertThat(accountBookFacade.unlockDateOf(rewardId).orElseThrow()).isEqualTo(unlockDate.toInstant());
        }
        assertThat(accountBookFacade.networksOf(rewardId)).isEqualTo(networks);
        reset(accountingObserver);
    }

    private void setupAccountingService() {
        accountBookEventStorage = new AccountBookEventStorageStub();
        accountingService = new AccountingService(new CachedAccountBookProvider(accountBookEventStorage), sponsorAccountStorage, currencyStorage,
                accountingObserver, projectAccountingObserver, invoiceStoragePort, accountBookObserver);
    }

    @BeforeEach
    void setup() {
        setupAccountingService();
        when(invoiceStoragePort.invoiceOf(any())).thenReturn(Optional.of(invoice));
    }

    @Nested
    class GivenAnUnknownCurrency {
        final Currency.Id currencyId = Currency.Id.random();
        final SponsorAccount.Id sponsorId = SponsorAccount.Id.random();
        final ProjectId projectId = ProjectId.random();

        @BeforeEach
        void setup() {
            setupAccountingService();
            when(currencyStorage.get(currencyId)).thenReturn(Optional.empty());
        }

        /*
         * Given a sponsor account
         * When I allocate money to a project in an unknown currency
         * Then The allocation is rejected
         */
        @Test
        void should_reject_allocation() {
            // When
            assertThatThrownBy(() -> accountingService.allocate(sponsorId, projectId, PositiveAmount.of(10L), currencyId))
                    // Then
                    .isInstanceOf(OnlyDustException.class).hasMessage("Currency %s not found".formatted(currencyId));

            assertThat(accountBookEventStorage.events).isEmpty();
        }

        /*
         * Given a sponsor that has allocated money to a project
         * When I refund money from the project in an unknown currency
         * Then The refund is rejected
         */
        @Test
        void should_reject_unallocation() {
            // When
            assertThatThrownBy(() -> accountingService.unallocate(projectId, sponsorId, PositiveAmount.of(10L), currencyId))
                    // Then
                    .isInstanceOf(OnlyDustException.class).hasMessage("Currency %s not found".formatted(currencyId));

            assertThat(accountBookEventStorage.events).isEmpty();
        }
    }

    @Nested
    class GivenNoSponsorAccount {
        final Currency currency = Currencies.USDC;
        final SponsorId sponsorId = SponsorId.random();
        final ProjectId projectId = ProjectId.random();

        @BeforeEach
        void setup() {
            setupAccountingService();
            when(currencyStorage.get(currency.id())).thenReturn(Optional.of(currency));
        }

        /*
         * Given a newly created sponsor
         * When I allocate money on it
         * Then A new sponsor account is created and the mint is registered
         */
        @Test
        void should_create_sponsor_account_and_mint_virtual_balance() {
            // Given
            final var amountToMint = PositiveAmount.of(10L);

            // When
            final var sponsorAccount = accountingService.createSponsorAccountWithInitialAllowance(sponsorId, currency.id(), null, amountToMint);

            // Then
            final var event = IdentifiedAccountBookEvent.of(1, new MintEvent(AccountId.of(sponsorAccount.account().id()), amountToMint));
            assertThat(accountBookEventStorage.events.get(currency)).contains(event);
            assertThat(sponsorAccount.account().unlockedBalance()).isEqualTo(Amount.ZERO);
            assertThat(sponsorAccount.account().currency()).isEqualTo(currency);
            assertThat(sponsorAccount.account().sponsorId()).isEqualTo(sponsorId);
            assertThat(sponsorAccount.account().network()).isEmpty();
            assertThat(sponsorAccount.account().lockedUntil()).isEmpty();
            assertThat(sponsorAccount.allowance()).isEqualTo(amountToMint);

            verify(accountBookObserver).on(event);
        }

        /*
         * Given a newly created sponsor
         * When I allocate money on it and provide a receipt
         * Then A new sponsor account is created, the mint is registered and the physical balance is updated
         */
        @Test
        void should_create_sponsor_account_and_funds_it() {
            // Given
            final var amount = Amount.of(10L);
            final var transaction = fakeTransaction(Network.ETHEREUM, amount);

            // When
            final var sponsorAccount = accountingService.createSponsorAccountWithInitialBalance(sponsorId, currency.id(), null, transaction);
            verify(accountingObserver).onSponsorAccountBalanceChanged(sponsorAccount);

            // Then
            final var event = IdentifiedAccountBookEvent.of(1, new MintEvent(AccountId.of(sponsorAccount.account().id()), PositiveAmount.of(amount)));
            assertThat(accountBookEventStorage.events.get(currency)).contains(event);
            assertThat(sponsorAccount.account().unlockedBalance()).isEqualTo(amount);
            assertThat(sponsorAccount.account().currency()).isEqualTo(currency);
            assertThat(sponsorAccount.account().sponsorId()).isEqualTo(sponsorId);
            assertThat(sponsorAccount.account().network()).contains(transaction.network());
            assertThat(sponsorAccount.account().getTransactions()).containsExactly(transaction);
            assertThat(sponsorAccount.account().lockedUntil()).isEmpty();

            final var savedAccount = sponsorAccountStorage.get(sponsorAccount.account().id()).orElseThrow();
            assertThat(savedAccount.id()).isEqualTo(sponsorAccount.account().id());
            assertThat(savedAccount.unlockedBalance()).isEqualTo(sponsorAccount.account().unlockedBalance());
            assertThat(savedAccount.currency()).isEqualTo(sponsorAccount.account().currency());
            assertThat(savedAccount.sponsorId()).isEqualTo(sponsorAccount.account().sponsorId());
            assertThat(savedAccount.network()).isEqualTo(sponsorAccount.account().network());
            assertThat(savedAccount.getTransactions()).isEqualTo(sponsorAccount.account().getTransactions());
            assertThat(savedAccount.lockedUntil()).isEqualTo(sponsorAccount.account().lockedUntil());

            verify(accountBookObserver).on(event);
        }

        /*
         * Given a newly created sponsor
         * When I create a locked sponsor account
         * Then The balance is always ZERO if the lock date is in the future
         */
        @Test
        void should_create_locked_sponsor_account_and_funds_it() {
            // Given
            final var amount = PositiveAmount.of(10L);
            final var transaction = fakeTransaction(Network.ETHEREUM, amount);
            final var lockedUntil = ZonedDateTime.now().plusDays(1);

            // When
            final var sponsorAccount = accountingService.createSponsorAccountWithInitialBalance(sponsorId, currency.id(), lockedUntil, transaction);
            verify(accountingObserver).onSponsorAccountBalanceChanged(sponsorAccount);

            // Then
            assertThat(accountBookEventStorage.events.get(currency))
                    .contains(IdentifiedAccountBookEvent.of(1, new MintEvent(AccountId.of(sponsorAccount.account().id()), amount)));
            assertThat(sponsorAccount.account().unlockedBalance()).isEqualTo(Amount.ZERO);
            assertThat(sponsorAccount.account().lockedUntil()).contains(lockedUntil.toInstant());

            final var savedAccount = sponsorAccountStorage.get(sponsorAccount.account().id()).orElseThrow();
            assertThat(savedAccount.unlockedBalance()).isEqualTo(Amount.ZERO);
            assertThat(savedAccount.lockedUntil()).isEqualTo(sponsorAccount.account().lockedUntil());
        }

        /*
         * Given a sponsor with no account
         * When I refund money from a project
         * Then The refund is rejected
         */
        @Test
        void should_reject_unallocation_when_no_sponsor_account_found() {
            // When
            assertThatThrownBy(() -> accountingService.unallocate(projectId, SponsorAccount.Id.random(), PositiveAmount.of(10L), currency.id()))
                    // Then
                    .isInstanceOf(OnlyDustException.class)
                    .hasMessageContaining("Cannot refund");

            assertThat(accountBookEventStorage.events).isEmpty();
        }

        /*
         * Given a newly created sponsor
         * When I allocate money on it and provide a receipt on a network that is not supported for the currency
         * Then The request is rejected
         */
        @Test
        void should_reject_funding_on_unsupported_currency() {
            // Given
            final var amount = PositiveAmount.of(10L);
            final var transaction = fakeTransaction(Network.STARKNET, amount);

            // When
            assertThatThrownBy(() -> accountingService.createSponsorAccountWithInitialBalance(sponsorId, currency.id(), null, transaction))
                    // Then
                    .isInstanceOf(OnlyDustException.class)
                    .hasMessageContaining("Currency USDC is not supported on network STARKNET");
        }
    }

    @Nested
    class GivenASponsorAccount {
        final Currency currency = Currencies.USDC;
        final Network network = Network.ETHEREUM;
        final SponsorId sponsorId = SponsorId.random();
        final ProjectId projectId1 = ProjectId.random();
        final ProjectId projectId2 = ProjectId.random();
        final RewardId rewardId1 = RewardId.random();
        final RewardId rewardId2 = RewardId.random();
        SponsorAccount sponsorAccount;

        @BeforeEach
        void setup() {
            setupAccountingService();
            when(currencyStorage.get(currency.id())).thenReturn(Optional.of(currency));
            when(currencyStorage.all()).thenReturn(Set.of(currency));
            sponsorAccount = accountingService.createSponsorAccountWithInitialAllowance(sponsorId, currency.id(), null, PositiveAmount.of(100L)).account();
        }

        /*
         * Given a sponsor with an account
         * When I allocate money to for the sponsor
         * Then The account allowance is updated
         */
        @Test
        void should_register_allowance() {
            // Given
            final var amount = PositiveAmount.of(faker.number().numberBetween(1L, 100L));

            // When
            {
                final var sponsorAccountStatement = accountingService.increaseAllowance(sponsorAccount.id(), amount);
                assertThat(sponsorAccountStatement.allowance()).isEqualTo(PositiveAmount.of(100L).add(amount));
            }
            {
                final var sponsorAccountStatement = accountingService.increaseAllowance(sponsorAccount.id(), amount.negate());
                assertThat(sponsorAccountStatement.allowance()).isEqualTo(PositiveAmount.of(100L));
            }

            // Then
            final var events = List.of(
                    IdentifiedAccountBookEvent.of(2, new MintEvent(AccountId.of(sponsorAccount.id()), amount)),
                    IdentifiedAccountBookEvent.of(3, new RefundEvent(AccountId.of(sponsorAccount.id()), AccountId.ROOT, amount))
            );
            assertThat(accountBookEventStorage.events.get(currency)).containsAll(events);
            events.forEach(e -> verify(accountBookObserver).on(e));
        }

        /*
         * Given a sponsor account
         * When I refund money from OnlyDust of more than I sent
         * Then The refund is rejected
         */
        @Test
        void should_reject_refund_when_not_enough_received() {
            // When
            assertThatThrownBy(() -> accountingService.increaseAllowance(sponsorAccount.id(), Amount.of(-110L)))
                    // Then
                    .isInstanceOf(OnlyDustException.class).hasMessageContaining("Cannot refund 110");
        }

        /*
         * Given a sponsor account
         * When I allocate money to a project
         * Then The transfer is registered from my account to the project
         */
        @Test
        void should_register_allocations_to_project() {
            // Given
            final var amount = PositiveAmount.of(faker.number().numberBetween(1L, 100L));

            // When
            accountingService.allocate(sponsorAccount.id(), projectId1, amount, currency.id());
            accountingService.unallocate(projectId1, sponsorAccount.id(), amount, currency.id());

            // Then
            final var events = List.of(
                    IdentifiedAccountBookEvent.of(2, new TransferEvent(AccountId.of(sponsorAccount.id()), AccountId.of(projectId1), amount)),
                    IdentifiedAccountBookEvent.of(3, new RefundEvent(AccountId.of(projectId1), AccountId.of(sponsorAccount.id()), amount))
            );
            assertThat(accountBookEventStorage.events.get(currency)).containsAll(events);
            events.forEach(e -> verify(accountBookObserver).on(e));
        }

        /*
         * Given a sponsor account
         * When I refund money from a project
         * Then The refund is rejected if the sponsor has not allocated enough money
         */
        @Test
        void should_reject_unallocation_when_not_enough_allocated() {
            // When
            assertThatThrownBy(() -> accountingService.unallocate(projectId1, sponsorAccount.id(), PositiveAmount.of(400L), currency.id()))
                    // Then
                    .isInstanceOf(OnlyDustException.class).hasMessageContaining("Cannot refund");
        }

        /*
         * Given a sponsor, a project and a contributor with a sponsor account
         * When the contributor is rewarded by the project but the sponsor is not funded (no real money received)
         * Then The contributor cannot withdraw his money
         */
        @Test
        void should_prevent_contributor_from_withdrawing_if_source_is_not_funded() {
            // When
            accountingService.allocate(sponsorAccount.id(), projectId1, PositiveAmount.of(10L), currency.id());
            accountingService.createReward(projectId1, rewardId1, PositiveAmount.of(10L), currency.id());
            assertOnRewardCreated(rewardId1, false, null, Set.of());

            assertThat(accountingService.isPayable(rewardId1, currency.id())).isFalse();
            final var transaction = fakeTransaction(network, PositiveAmount.of(10L));
            assertThatThrownBy(() -> accountingService.pay(rewardId1, transaction))
                    // Then
                    .isInstanceOf(OnlyDustException.class)
                    .hasMessage("Reward %s is not payable on ETHEREUM".formatted(rewardId1));
        }

        /*
         * Given a sponsor account
         * When I fund money from a project without minting the corresponding amount
         * Then I can withdraw this money
         */
        @Test
        void should_fund_and_withdraw() {
            final var amount = Amount.of(faker.number().randomNumber());

            // When
            accountingService.fund(sponsorAccount.id(), fakeTransaction(network, amount));

            // Then
            assertThat(sponsorAccountStorage.get(sponsorAccount.id()).orElseThrow().unlockedBalance()).isEqualTo(amount);

            // When
            accountingService.fund(sponsorAccount.id(), fakeTransaction(network, amount.negate()));

            // Then
            verify(accountingObserver, times(2)).onSponsorAccountBalanceChanged(any());
            assertThat(sponsorAccountStorage.get(sponsorAccount.id()).orElseThrow().unlockedBalance()).isEqualTo(Amount.ZERO);
        }

        /*
         * Given a sponsor account
         * When I register a transaction
         * Then I can delete it
         */
        @Test
        void should_fund_and_remove_transaction() {
            final var amount = Amount.of(faker.number().randomNumber());
            final var transaction = fakeTransaction(network, amount);

            // When
            accountingService.fund(sponsorAccount.id(), transaction);
            verify(accountingObserver).onSponsorAccountBalanceChanged(any());
            // Then
            assertThat(sponsorAccountStorage.get(sponsorAccount.id()).orElseThrow().unlockedBalance()).isEqualTo(amount);

            // When
            reset(accountingObserver);
            accountingService.delete(sponsorAccount.id(), transaction.id());
            // Then
            assertThat(sponsorAccountStorage.get(sponsorAccount.id()).orElseThrow().unlockedBalance()).isEqualTo(Amount.ZERO);
            verify(accountingObserver).onSponsorAccountBalanceChanged(any());
        }

        /*
         * Given a sponsor, a project and a contributor
         * When
         *    - the sponsor funds its account in multiple times
         *    - project 1 rewards the contributor with the full amount
         * Then, the contributor can withdraw his money
         */
        @Test
        void should_allow_multiple_times_funding() {
            // When
            accountingService.fund(sponsorAccount.id(), fakeTransaction(network, PositiveAmount.of(30L)));
            accountingService.fund(sponsorAccount.id(), fakeTransaction(network, PositiveAmount.of(30L)));
            accountingService.fund(sponsorAccount.id(), fakeTransaction(network, PositiveAmount.of(40L)));
            verify(accountingObserver, times(3)).onSponsorAccountBalanceChanged(any());

            accountingService.allocate(sponsorAccount.id(), projectId2, PositiveAmount.of(100L), currency.id());
            accountingService.createReward(projectId2, rewardId2, PositiveAmount.of(100L), currency.id());
            assertOnRewardCreated(rewardId2, true, null, Set.of(network));

            accountingService.pay(rewardId2, fakePaymentReference(Network.ETHEREUM));

            // Then
            final var events = List.of(
                    IdentifiedAccountBookEvent.of(2, new TransferEvent(AccountId.of(sponsorAccount.id()), AccountId.of(projectId2), PositiveAmount.of(100L))),
                    IdentifiedAccountBookEvent.of(3, new TransferEvent(AccountId.of(projectId2), AccountId.of(rewardId2), PositiveAmount.of(100L)))
            );
            assertThat(accountBookEventStorage.events.get(currency)).containsAll(events);
            events.forEach(e -> verify(accountBookObserver).on(e));

            assertThat(sponsorAccountStorage.get(sponsorAccount.id()).orElseThrow().unlockedBalance()).isEqualTo(Amount.ZERO);
        }

        /*
         * Given a sponsor, a project and a contributor
         * When
         *    - the sponsor funds its account partially
         *    - project 1 rewards the contributor several times
         * Then, the contributor cannot withdraw his money beyond funding
         */
        @Test
        void should_reject_withdraw_more_than_funded() {
            // When
            accountingService.fund(sponsorAccount.id(), fakeTransaction(network, PositiveAmount.of(50L)));

            accountingService.allocate(sponsorAccount.id(), projectId2, PositiveAmount.of(80L), currency.id());
            accountingService.createReward(projectId2, rewardId1, PositiveAmount.of(40L), currency.id());
            assertOnRewardCreated(rewardId1, true, null, Set.of(network));

            accountingService.createReward(projectId2, rewardId2, PositiveAmount.of(40L), currency.id());
            assertOnRewardCreated(rewardId2, true, null, Set.of(network));

            {
                final var account = accountingService.getSponsorAccountStatement(sponsorAccount.id()).orElseThrow();
                assertThat(account.awaitingPaymentAmount()).isEqualTo(PositiveAmount.of(80L));
            }
            assertThat(accountingService.isPayable(rewardId1, currency.id())).isTrue();
            assertThat(accountingService.isPayable(rewardId2, currency.id())).isTrue();

            accountingService.pay(rewardId1, fakeTransaction(network, PositiveAmount.of(50L)));

            {
                final var account = accountingService.getSponsorAccountStatement(sponsorAccount.id()).orElseThrow();
                assertThat(account.awaitingPaymentAmount()).isEqualTo(PositiveAmount.of(40L));
                assertThat(account.account().balance()).isEqualTo(Amount.of(10L));
                assertThat(account.allowance()).isEqualTo(PositiveAmount.of(20L));
            }

            assertThat(accountingService.isPayable(rewardId2, currency.id())).isFalse();
            assertThatThrownBy(() -> accountingService.pay(rewardId2, fakeTransaction(network, PositiveAmount.of(50L))))
                    // Then
                    .isInstanceOf(OnlyDustException.class)
                    .hasMessage("Reward %s is not payable on ETHEREUM".formatted(rewardId2));
        }

        /*
         * Given a sponsor account
         * When I fund it on a given network
         * Then I cannot fund it on a different network
         */
        @Test
        void should_forbid_different_networks_on_same_account() {
            // Given
            accountingService.fund(sponsorAccount.id(), fakeTransaction(Network.ETHEREUM, PositiveAmount.of(100L)));

            // When
            assertThatThrownBy(() -> accountingService.fund(sponsorAccount.id(), fakeTransaction(Network.STARKNET, PositiveAmount.of(100L))))
                    // Then
                    .isInstanceOf(OnlyDustException.class)
                    .hasMessageContaining("Cannot mix transactions from different networks");
        }

        @Test
        void should_cancel_a_reward() {
            // Given
            accountingService.allocate(sponsorAccount.id(), projectId1, PositiveAmount.of(100L), currency.id());
            accountingService.createReward(projectId1, rewardId1, PositiveAmount.of(40L), currency.id());
            verify(projectAccountingObserver).onAllowanceUpdated(projectId1, currency.id(), PositiveAmount.of(60L), PositiveAmount.of(100L));
            reset(projectAccountingObserver);

            // When
            accountingService.cancel(rewardId1, currency.id());

            // Then
            verify(projectAccountingObserver).onAllowanceUpdated(projectId1, currency.id(), PositiveAmount.of(100L), PositiveAmount.of(100L));
            final var events = List.of(
                    IdentifiedAccountBookEvent.of(2, new TransferEvent(AccountId.of(sponsorAccount.id()), AccountId.of(projectId1), PositiveAmount.of(100L))),
                    IdentifiedAccountBookEvent.of(3, new TransferEvent(AccountId.of(projectId1), AccountId.of(rewardId1), PositiveAmount.of(40L))),
                    IdentifiedAccountBookEvent.of(4, new FullRefundEvent(AccountId.of(rewardId1)))
            );
            assertThat(accountBookEventStorage.events.get(currency)).containsAll(events);
            events.forEach(e -> verify(accountBookObserver).on(e));
        }

        @Test
        void should_prevent_a_reward_from_being_payable_if_already_paid() {
            // Given
            accountingService.fund(sponsorAccount.id(), fakeTransaction(network, PositiveAmount.of(100L)));
            accountingService.allocate(sponsorAccount.id(), projectId1, PositiveAmount.of(100L), currency.id());
            accountingService.createReward(projectId1, rewardId1, PositiveAmount.of(40L), currency.id());
            when(currencyStorage.all()).thenReturn(Set.of(currency));

            // When
            final var payments = accountingService.pay(Set.of(rewardId1));

            // Then
            assertThat(payments).hasSize(1);
            assertThat(accountingService.isPayable(rewardId1, currency.id())).isFalse();
            assertThat(accountingService.getPayableRewards(Set.of(rewardId1))).isEmpty();
            assertThatThrownBy(() -> accountingService.cancel(rewardId1, currency.id()))
                    .isInstanceOf(OnlyDustException.class)
                    .hasMessageContaining("Cannot entirely refund");
            assertThatThrownBy(() -> accountingService.pay(rewardId1, fakePaymentReference(network)))
                    .isInstanceOf(OnlyDustException.class)
                    .hasMessageContaining("Reward %s is not payable".formatted(rewardId1));

            // When
            accountingService.cancel(payments.get(0));

            // Then
            assertThat(accountingService.isPayable(rewardId1, currency.id())).isTrue();
            assertThat(accountingService.getPayableRewards(Set.of(rewardId1))).hasSize(1);
        }
    }

    @Nested
    class GivenALockedSponsorAccount {
        final Currency currency = Currencies.USDC;
        final Network network = Network.ETHEREUM;
        final SponsorId sponsorId = SponsorId.random();
        final ProjectId projectId1 = ProjectId.random();
        final ProjectId projectId2 = ProjectId.random();
        final RewardId rewardId1 = RewardId.random();
        final RewardId rewardId2 = RewardId.random();
        final ZonedDateTime unlockDate = ZonedDateTime.now().plusDays(1);
        SponsorAccount sponsorAccount;

        @BeforeEach
        void setup() {
            setupAccountingService();
            when(currencyStorage.get(currency.id())).thenReturn(Optional.of(currency));
            sponsorAccount = accountingService.createSponsorAccountWithInitialAllowance(sponsorId, currency.id(), unlockDate, PositiveAmount.of(300L))
                    .account();
        }

        /*
         * Given a sponsor with a locked account
         * When I allocate money to for the sponsor
         * Then The account allowance is updated
         */
        @Test
        void should_register_allowance() {
            // Given
            final var amount = PositiveAmount.of(faker.number().numberBetween(1L, 100L));

            // When
            accountingService.increaseAllowance(sponsorAccount.id(), amount);
            accountingService.increaseAllowance(sponsorAccount.id(), amount.negate());

            // Then
            final var events = List.of(
                    IdentifiedAccountBookEvent.of(2, new MintEvent(AccountId.of(sponsorAccount.id()), amount)),
                    IdentifiedAccountBookEvent.of(3, new RefundEvent(AccountId.of(sponsorAccount.id()), AccountId.ROOT, amount))
            );
            assertThat(accountBookEventStorage.events.get(currency)).containsAll(events);
            events.forEach(e -> verify(accountBookObserver).on(e));
        }

        /*
         * Given a sponsor with a locked account
         * When I allocate money to a project
         * Then The transfer is registered from my account to the project
         */
        @Test
        void should_register_allocations_to_project() {
            // Given
            final var amount = PositiveAmount.of(faker.number().numberBetween(1L, 100L));

            // When
            accountingService.allocate(sponsorAccount.id(), projectId1, amount, currency.id());
            accountingService.unallocate(projectId1, sponsorAccount.id(), amount, currency.id());

            // Then
            final var events = List.of(
                    IdentifiedAccountBookEvent.of(2, new TransferEvent(AccountId.of(sponsorAccount.id()), AccountId.of(projectId1), amount)),
                    IdentifiedAccountBookEvent.of(3, new RefundEvent(AccountId.of(projectId1), AccountId.of(sponsorAccount.id()), amount))
            );
            assertThat(accountBookEventStorage.events.get(currency)).containsAll(events);
            events.forEach(e -> verify(accountBookObserver).on(e));
        }

        /*
         * Given a sponsor with a locked account
         * When I refund money from a project
         * Then The refund is rejected if the sponsor has not allocated enough money
         */
        @Test
        void should_reject_unallocation_when_not_enough_allocated() {
            // When
            assertThatThrownBy(() -> accountingService.unallocate(projectId1, sponsorAccount.id(), PositiveAmount.of(400L), currency.id()))
                    // Then
                    .isInstanceOf(OnlyDustException.class).hasMessageContaining("Cannot refund");
        }

        /*
         * Given a sponsor, a project and a contributor with a locked account
         * When the contributor is rewarded by the project but the sponsor is not funded (no real money received)
         * Then The contributor cannot withdraw his money
         */
        @Test
        void should_prevent_contributor_from_withdrawing_if_source_is_not_funded() {
            // When
            accountingService.allocate(sponsorAccount.id(), projectId1, PositiveAmount.of(10L), currency.id());
            accountingService.createReward(projectId1, rewardId1, PositiveAmount.of(10L), currency.id());
            assertOnRewardCreated(rewardId1, false, unlockDate, Set.of());

            assertThat(accountingService.isPayable(rewardId1, currency.id())).isFalse();
            final var transaction = fakeTransaction(network, PositiveAmount.of(10L));
            assertThatThrownBy(() -> accountingService.pay(rewardId1, transaction))
                    // Then
                    .isInstanceOf(OnlyDustException.class)
                    .hasMessage("Reward %s is not payable on ETHEREUM".formatted(rewardId1));
        }

        /*
         * Given a sponsor, a project and a contributor
         * When
         *    - the sponsor funds its account in multiple times
         *    - project 1 rewards the contributor with the full amount
         * Then, the contributor can withdraw his money
         */
        @Test
        void should_allow_multiple_times_funding() {
            // When
            accountingService.fund(sponsorAccount.id(), fakeTransaction(network, PositiveAmount.of(30L)));
            accountingService.fund(sponsorAccount.id(), fakeTransaction(network, PositiveAmount.of(30L)));
            accountingService.fund(sponsorAccount.id(), fakeTransaction(network, PositiveAmount.of(40L)));

            accountingService.allocate(sponsorAccount.id(), projectId2, PositiveAmount.of(100L), currency.id());
            accountingService.createReward(projectId2, rewardId2, PositiveAmount.of(100L), currency.id());
            assertOnRewardCreated(rewardId2, true, unlockDate, Set.of(network));

            // Then
            final var events = List.of(
                    IdentifiedAccountBookEvent.of(2, new TransferEvent(AccountId.of(sponsorAccount.id()), AccountId.of(projectId2), PositiveAmount.of(100L))),
                    IdentifiedAccountBookEvent.of(3, new TransferEvent(AccountId.of(projectId2), AccountId.of(rewardId2), PositiveAmount.of(100L)))
            );
            assertThat(accountBookEventStorage.events.get(currency)).containsAll(events);
            events.forEach(e -> verify(accountBookObserver).on(e));

            assertThat(sponsorAccountStorage.get(sponsorAccount.id()).orElseThrow().unlockedBalance()).isEqualTo(Amount.ZERO);
        }

        /*
         * Given a sponsor, a project and a contributor
         * When
         *    - the sponsor funds its account partially
         *    - project 1 rewards the contributor several times
         * Then, the contributor cannot withdraw his money beyond funding
         */
        @Test
        void should_reject_withdraw() {
            // Given
            final var amount = PositiveAmount.of(faker.number().randomNumber());

            // When
            accountingService.fund(sponsorAccount.id(), fakeTransaction(network, amount));
            accountingService.increaseAllowance(sponsorAccount.id(), amount);

            accountingService.allocate(sponsorAccount.id(), projectId1, amount, currency.id());
            accountingService.createReward(projectId1, rewardId1, amount, currency.id());
            assertOnRewardCreated(rewardId1, true, unlockDate, Set.of(network));

            assertThat(accountingService.isPayable(rewardId1, currency.id())).isFalse();
            assertThatThrownBy(() -> accountingService.pay(rewardId1, fakeTransaction(network, amount)))
                    // Then
                    .isInstanceOf(OnlyDustException.class)
                    .hasMessage("Reward %s is not payable on ETHEREUM".formatted(rewardId1));
        }

        /*
         * Given a sponsor account
         * When I update the unlock date
         * Then the unlock date is updated
         */
        @Test
        void should_update_unlock_date() {
            // Given
            final var unlockDate = ZonedDateTime.now().plusDays(2);

            // When
            final var newSponsorAccount = accountingService.updateSponsorAccount(sponsorAccount.id(), unlockDate);
            verify(accountingObserver).onSponsorAccountUpdated(newSponsorAccount);

            // Then
            assertThat(newSponsorAccount.account().lockedUntil()).contains(unlockDate.toInstant());
            assertThat(sponsorAccountStorage.get(sponsorAccount.id()).orElseThrow().lockedUntil()).contains(unlockDate.toInstant());
        }
    }

    @Nested
    class GivenSeveralSponsorAccounts {
        final SponsorId sponsorId = SponsorId.random();
        final ZonedDateTime unlockDate = ZonedDateTime.now().plusDays(1);
        final ProjectId projectId = ProjectId.random();
        final RewardId rewardId = RewardId.random();
        final RewardId rewardId2 = RewardId.random();
        Currency currency;
        SponsorAccount unlockedSponsorSponsorAccount1;
        SponsorAccount unlockedSponsorSponsorAccount2;
        SponsorAccount lockedSponsorSponsorAccount;

        @BeforeEach
        void setup() {
            setupAccountingService();
            currency = Currency.of(ERC20Tokens.ETH_USDC); // build a copy of USDC currency to avoid side effects
            currency.erc20().add(ERC20Tokens.OP_USDC);

            when(currencyStorage.get(currency.id())).thenReturn(Optional.of(currency));
            when(currencyStorage.all()).thenReturn(Set.of(currency));
            unlockedSponsorSponsorAccount1 = accountingService.createSponsorAccountWithInitialAllowance(sponsorId, currency.id(), null,
                    PositiveAmount.of(100L)).account();
            unlockedSponsorSponsorAccount2 = accountingService.createSponsorAccountWithInitialAllowance(sponsorId, currency.id(), null,
                    PositiveAmount.of(100L)).account();
            lockedSponsorSponsorAccount = accountingService.createSponsorAccountWithInitialAllowance(sponsorId, currency.id(), unlockDate,
                    PositiveAmount.of(100L)).account();
        }

        /*
         * Given 2 sponsor accounts
         * When Only the first account is funded
         * Then The contributor paid by the other account cannot withdraw his money
         */
        @Test
        void should_prevent_contributor_from_withdrawing_if_source_is_not_funded() {
            // Given
            accountingService.allocate(unlockedSponsorSponsorAccount1.id(), projectId, PositiveAmount.of(100L), currency.id());
            accountingService.allocate(unlockedSponsorSponsorAccount2.id(), projectId, PositiveAmount.of(100L), currency.id());
            accountingService.createReward(projectId, rewardId, PositiveAmount.of(200L), currency.id());
            assertOnRewardCreated(rewardId, false, null, Set.of());

            // When
            final var account = accountingService.fund(unlockedSponsorSponsorAccount1.id(), fakeTransaction(Network.ETHEREUM, PositiveAmount.of(100L)));
            assertThat(account.awaitingPaymentAmount()).isEqualTo(PositiveAmount.of(100L));

            assertThat(accountingService.isPayable(rewardId, currency.id())).isFalse();
            assertThatThrownBy(() -> accountingService.pay(rewardId, fakeTransaction(Network.ETHEREUM, PositiveAmount.of(100L))))
                    // Then
                    .isInstanceOf(OnlyDustException.class)
                    .hasMessage("Reward %s is not payable on ETHEREUM".formatted(rewardId));
        }

        /*
         * Given 2 sponsor accounts that rewarded 2 contributors via the same project
         * When Only sponsor account 1 funds its account
         * Then Only the first contributor can withdraw its reward
         */
        @Test
        void should_allow_contributor_to_withdraw_only_what_is_funded() {
            // Given
            accountingService.allocate(unlockedSponsorSponsorAccount1.id(), projectId, PositiveAmount.of(100L), currency.id());
            accountingService.allocate(unlockedSponsorSponsorAccount2.id(), projectId, PositiveAmount.of(100L), currency.id());
            accountingService.createReward(projectId, rewardId, PositiveAmount.of(100L), currency.id());
            assertOnRewardCreated(rewardId, false, null, Set.of());

            accountingService.createReward(projectId, rewardId2, PositiveAmount.of(100L), currency.id());
            assertOnRewardCreated(rewardId2, false, null, Set.of());

            // When
            accountingService.fund(unlockedSponsorSponsorAccount1.id(), fakeTransaction(Network.ETHEREUM, PositiveAmount.of(100L)));

            assertThat(accountingService.isPayable(rewardId, currency.id())).isTrue();
            assertThat(accountingService.isPayable(rewardId2, currency.id())).isFalse();

            {
                final var account = accountingService.getSponsorAccountStatement(unlockedSponsorSponsorAccount1.id()).orElseThrow();
                assertThat(account.awaitingPaymentAmount()).isEqualTo(PositiveAmount.of(100L));
            }
            accountingService.pay(rewardId, fakeTransaction(Network.ETHEREUM, PositiveAmount.of(1000L)));
            {
                final var account = accountingService.getSponsorAccountStatement(unlockedSponsorSponsorAccount1.id()).orElseThrow();
                assertThat(account.awaitingPaymentAmount()).isEqualTo(PositiveAmount.ZERO);
            }

            // Then
            assertThat(accountingService.isPayable(rewardId2, currency.id())).isFalse();
            assertThatThrownBy(() -> accountingService.pay(rewardId2, fakeTransaction(Network.ETHEREUM, PositiveAmount.of(1000L))))
                    // Then
                    .isInstanceOf(OnlyDustException.class)
                    .hasMessage("Reward %s is not payable on ETHEREUM".formatted(rewardId2));
        }

        /*
         * Given a locked sponsor account and another unlocked sponsor
         * When A reward is created from both accounts
         * Then The reward is not payable
         */
        @Test
        void should_not_pay_partially_locked_rewards() {
            // Given
            accountingService.fund(unlockedSponsorSponsorAccount1.id(), fakeTransaction(Network.ETHEREUM, PositiveAmount.of(100L)));
            accountingService.fund(lockedSponsorSponsorAccount.id(), fakeTransaction(Network.ETHEREUM, PositiveAmount.of(100L)));

            accountingService.allocate(unlockedSponsorSponsorAccount1.id(), projectId, PositiveAmount.of(100L), currency.id());
            accountingService.allocate(lockedSponsorSponsorAccount.id(), projectId, PositiveAmount.of(100L), currency.id());
            accountingService.createReward(projectId, rewardId, PositiveAmount.of(200L), currency.id());
            assertOnRewardCreated(rewardId, true, unlockDate, Set.of(Network.ETHEREUM));

            // When
            assertThat(accountingService.isPayable(rewardId, currency.id())).isFalse();
            assertThatThrownBy(() -> accountingService.pay(rewardId, fakePaymentReference(Network.ETHEREUM)))
                    // Then
                    .isInstanceOf(OnlyDustException.class)
                    .hasMessage("Reward %s is not payable on ETHEREUM".formatted(rewardId));
        }

        /*
         * Given sponsor 1 that funded its account on Ethereum and sponsor 2 that funded its account on Optimism
         * When A contributor is rewarded by the project
         * Then The contributor can withdraw his money on both networks
         */
        @Test
        void should_withdraw_on_both_networks() {
            // Given
            accountingService.fund(unlockedSponsorSponsorAccount1.id(), fakeTransaction(Network.ETHEREUM, PositiveAmount.of(200L)));
            accountingService.fund(unlockedSponsorSponsorAccount2.id(), fakeTransaction(Network.OPTIMISM, PositiveAmount.of(100L)));

            accountingService.increaseAllowance(unlockedSponsorSponsorAccount1.id(), PositiveAmount.of(200L));
            accountingService.increaseAllowance(unlockedSponsorSponsorAccount2.id(), PositiveAmount.of(100L));
            accountingService.allocate(unlockedSponsorSponsorAccount1.id(), projectId, PositiveAmount.of(200L), currency.id());
            accountingService.allocate(unlockedSponsorSponsorAccount2.id(), projectId, PositiveAmount.of(100L), currency.id());
            accountingService.createReward(projectId, rewardId, PositiveAmount.of(300L), currency.id());
            assertOnRewardCreated(rewardId, true, null, Set.of(Network.ETHEREUM, Network.OPTIMISM));

            assertThat(sponsorAccountStorage.get(unlockedSponsorSponsorAccount1.id()).orElseThrow().unlockedBalance()).isEqualTo(Amount.of(200L));
            assertThat(sponsorAccountStorage.get(unlockedSponsorSponsorAccount2.id()).orElseThrow().unlockedBalance()).isEqualTo(Amount.of(100L));

            // When
            assertThat(accountingService.isPayable(rewardId, currency.id())).isTrue();

            accountingService.pay(rewardId, fakePaymentReference(Network.ETHEREUM));
            accountingService.pay(rewardId, fakePaymentReference(Network.OPTIMISM));

            // Then
            assertThat(sponsorAccountStorage.get(unlockedSponsorSponsorAccount1.id()).orElseThrow().unlockedBalance()).isEqualTo(Amount.ZERO);
            assertThat(sponsorAccountStorage.get(unlockedSponsorSponsorAccount2.id()).orElseThrow().unlockedBalance()).isEqualTo(Amount.ZERO);
        }

        /*
         * Given 2 sponsors that funded their account on Ethereum
         * When A contributor is rewarded by the project
         * Then The contributor can withdraw his money from both sponsor accounts
         */
        @Test
        void should_withdraw_on_both_sponsor_accounts() {
            // Given
            accountingService.fund(unlockedSponsorSponsorAccount1.id(), fakeTransaction(Network.ETHEREUM, PositiveAmount.of(200L)));
            accountingService.fund(unlockedSponsorSponsorAccount2.id(), fakeTransaction(Network.ETHEREUM, PositiveAmount.of(100L)));

            accountingService.increaseAllowance(unlockedSponsorSponsorAccount1.id(), PositiveAmount.of(200L));
            accountingService.increaseAllowance(unlockedSponsorSponsorAccount2.id(), PositiveAmount.of(100L));
            accountingService.allocate(unlockedSponsorSponsorAccount1.id(), projectId, PositiveAmount.of(200L), currency.id());
            accountingService.allocate(unlockedSponsorSponsorAccount2.id(), projectId, PositiveAmount.of(100L), currency.id());
            accountingService.createReward(projectId, rewardId, PositiveAmount.of(300L), currency.id());
            assertOnRewardCreated(rewardId, true, null, Set.of(Network.ETHEREUM));

            assertThat(sponsorAccountStorage.get(unlockedSponsorSponsorAccount1.id()).orElseThrow().unlockedBalance()).isEqualTo(Amount.of(200L));
            assertThat(sponsorAccountStorage.get(unlockedSponsorSponsorAccount2.id()).orElseThrow().unlockedBalance()).isEqualTo(Amount.of(100L));

            // When
            assertThat(accountingService.isPayable(rewardId, currency.id())).isTrue();

            reset(accountingObserver);
            final var reference = fakePaymentReference(Network.ETHEREUM);
            accountingService.pay(rewardId, reference);
            verify(accountingObserver, times(2)).onSponsorAccountBalanceChanged(any());
            verify(accountingObserver).onPaymentReceived(rewardId, reference);
            verify(accountingObserver).onRewardPaid(rewardId);

            // Then
            assertThat(sponsorAccountStorage.get(unlockedSponsorSponsorAccount1.id()).orElseThrow().unlockedBalance()).isEqualTo(Amount.ZERO);
            assertThat(sponsorAccountStorage.get(unlockedSponsorSponsorAccount2.id()).orElseThrow().unlockedBalance()).isEqualTo(Amount.ZERO);
        }

        @Test
        void should_cancel_a_reward() {
            // Given
            accountingService.increaseAllowance(unlockedSponsorSponsorAccount1.id(), PositiveAmount.of(200L));
            accountingService.increaseAllowance(unlockedSponsorSponsorAccount2.id(), PositiveAmount.of(100L));
            accountingService.allocate(unlockedSponsorSponsorAccount1.id(), projectId, PositiveAmount.of(200L), currency.id());
            verify(projectAccountingObserver).onAllowanceUpdated(projectId, currency.id(), PositiveAmount.of(200L), PositiveAmount.of(200L));
            reset(projectAccountingObserver);

            accountingService.allocate(unlockedSponsorSponsorAccount2.id(), projectId, PositiveAmount.of(100L), currency.id());
            verify(projectAccountingObserver).onAllowanceUpdated(projectId, currency.id(), PositiveAmount.of(300L), PositiveAmount.of(300L));
            reset(projectAccountingObserver);

            accountingService.createReward(projectId, rewardId, PositiveAmount.of(250L), currency.id());
            verify(projectAccountingObserver).onAllowanceUpdated(projectId, currency.id(), PositiveAmount.of(50L), PositiveAmount.of(300L));
            reset(projectAccountingObserver);

            // When
            accountingService.cancel(rewardId, currency.id());

            // Then
            verify(projectAccountingObserver).onAllowanceUpdated(projectId, currency.id(), PositiveAmount.of(300L), PositiveAmount.of(300L));
            final var events = List.of(
                    IdentifiedAccountBookEvent.of(6, new TransferEvent(AccountId.of(unlockedSponsorSponsorAccount1.id()), AccountId.of(projectId),
                            PositiveAmount.of(200L))),
                    IdentifiedAccountBookEvent.of(7, new TransferEvent(AccountId.of(unlockedSponsorSponsorAccount2.id()), AccountId.of(projectId),
                            PositiveAmount.of(100L))),
                    IdentifiedAccountBookEvent.of(8, new TransferEvent(AccountId.of(projectId), AccountId.of(rewardId), PositiveAmount.of(250L))),
                    IdentifiedAccountBookEvent.of(9, new FullRefundEvent(AccountId.of(rewardId)))
            );
            assertThat(accountBookEventStorage.events.get(currency)).containsAll(events);
            events.forEach(e -> verify(accountBookObserver).on(e));
        }
    }

    @Nested
    class GivenAProjectWithBudget {
        final Currency usdc = Currency.of(ERC20Tokens.ETH_USDC); // build a copy of USDC currency to avoid side effects
        final Currency op = Currencies.OP;
        final SponsorId sponsorId = SponsorId.random();
        final ProjectId projectId = ProjectId.random();
        final RewardId rewardId1 = RewardId.random();
        final RewardId rewardId2 = RewardId.random();
        final RewardId rewardId3 = RewardId.random();
        final RewardId rewardId4 = RewardId.random();
        final RewardId rewardId5 = RewardId.random();
        final RewardId rewardId6 = RewardId.random();
        SponsorAccount unlockedSponsorAccountUsdc1;
        SponsorAccount unlockedSponsorAccountUsdc2;
        SponsorAccount unlockedSponsorAccountOp;
        SponsorAccount lockedSponsorAccountUsdc;

        @BeforeEach
        void setup() {
            setupAccountingService();
            usdc.erc20().add(ERC20Tokens.OP_USDC);

            when(currencyStorage.get(usdc.id())).thenReturn(Optional.of(usdc));
            when(currencyStorage.get(op.id())).thenReturn(Optional.of(op));
            when(currencyStorage.all()).thenReturn(Set.of(usdc, op));

            final var lockDate = ZonedDateTime.now().plusDays(1);

            unlockedSponsorAccountUsdc1 =
                    accountingService.createSponsorAccountWithInitialAllowance(sponsorId, usdc.id(), null, PositiveAmount.of(200L)).account();
            unlockedSponsorAccountUsdc2 =
                    accountingService.createSponsorAccountWithInitialAllowance(sponsorId, usdc.id(), null, PositiveAmount.of(100L)).account();
            unlockedSponsorAccountOp = accountingService.createSponsorAccountWithInitialAllowance(sponsorId, op.id(), null, PositiveAmount.of(100L)).account();
            lockedSponsorAccountUsdc =
                    accountingService.createSponsorAccountWithInitialAllowance(sponsorId, usdc.id(), lockDate, PositiveAmount.of(100L)).account();

            accountingService.allocate(unlockedSponsorAccountUsdc1.id(), projectId, PositiveAmount.of(200L), usdc.id());
            accountingService.allocate(unlockedSponsorAccountUsdc2.id(), projectId, PositiveAmount.of(100L), usdc.id());
            accountingService.allocate(unlockedSponsorAccountOp.id(), projectId, PositiveAmount.of(100L), op.id());
            accountingService.allocate(lockedSponsorAccountUsdc.id(), projectId, PositiveAmount.of(100L), usdc.id());
            verify(projectAccountingObserver).onAllowanceUpdated(projectId, usdc.id(), PositiveAmount.of(400L), PositiveAmount.of(400L));
            verify(projectAccountingObserver).onAllowanceUpdated(projectId, op.id(), PositiveAmount.of(100L), PositiveAmount.of(100L));

            accountingService.createReward(projectId, rewardId1, PositiveAmount.of(75L), usdc.id());
            assertOnRewardCreated(rewardId1, false, null, Set.of());
            verify(projectAccountingObserver).onAllowanceUpdated(projectId, usdc.id(), PositiveAmount.of(325L), PositiveAmount.of(400L));

            accountingService.createReward(projectId, rewardId2, PositiveAmount.of(75L), usdc.id());
            assertOnRewardCreated(rewardId2, false, null, Set.of());
            verify(projectAccountingObserver).onAllowanceUpdated(projectId, usdc.id(), PositiveAmount.of(250L), PositiveAmount.of(400L));

            accountingService.createReward(projectId, rewardId3, PositiveAmount.of(75L), usdc.id());
            assertOnRewardCreated(rewardId3, false, null, Set.of());
            verify(projectAccountingObserver).onAllowanceUpdated(projectId, usdc.id(), PositiveAmount.of(175L), PositiveAmount.of(400L));

            accountingService.createReward(projectId, rewardId4, PositiveAmount.of(75L), usdc.id());
            assertOnRewardCreated(rewardId4, false, null, Set.of());
            verify(projectAccountingObserver).onAllowanceUpdated(projectId, usdc.id(), PositiveAmount.of(100L), PositiveAmount.of(400L));

            accountingService.createReward(projectId, rewardId5, PositiveAmount.of(90L), op.id());
            assertOnRewardCreated(rewardId5, false, null, Set.of());
            verify(projectAccountingObserver).onAllowanceUpdated(projectId, op.id(), PositiveAmount.of(10L), PositiveAmount.of(100L));

            accountingService.createReward(projectId, rewardId6, PositiveAmount.of(75L), usdc.id());
            assertOnRewardCreated(rewardId6, false, lockDate, Set.of());
            verify(projectAccountingObserver).onAllowanceUpdated(projectId, usdc.id(), PositiveAmount.of(25L), PositiveAmount.of(400L));
        }

        /*
         * Given a project with a budget
         * When I reward contributors
         * Then We can list the payable rewards
         */
        @Test
        void should_return_no_payable_reward_if_none_fund() {
            // When
            final var payableRewards = accountingService.getPayableRewards(Set.of(rewardId1));

            // Then
            assertThat(payableRewards).isEmpty();
        }

        @Test
        void should_return_payable_rewards_on_one_currency_and_one_network() {
            // Given
            accountingService.fund(unlockedSponsorAccountUsdc1.id(), fakeTransaction(Network.ETHEREUM, PositiveAmount.of(150L)));
            {
                // When
                final var payableRewards = accountingService.getPayableRewards(Set.of(rewardId1, rewardId2, rewardId3, rewardId4, rewardId5, rewardId6));

                // Then
                assertThat(payableRewards).containsExactlyInAnyOrder(
                        new PayableReward(rewardId1, usdc.forNetwork(Network.ETHEREUM), PositiveAmount.of(75L)),
                        new PayableReward(rewardId2, usdc.forNetwork(Network.ETHEREUM), PositiveAmount.of(75L))
                );
            }
            {
                // When
                final var payableRewards = accountingService.getPayableRewards(Set.of(rewardId1, rewardId2, rewardId3));
                final var payableRewards1 = accountingService.getPayableRewards(Set.of(rewardId1));
                final var payableRewards2 = accountingService.getPayableRewards(Set.of(rewardId2));

                // Then
                assertThat(payableRewards).containsExactlyInAnyOrder(
                        new PayableReward(rewardId1, usdc.forNetwork(Network.ETHEREUM), PositiveAmount.of(75L)),
                        new PayableReward(rewardId2, usdc.forNetwork(Network.ETHEREUM), PositiveAmount.of(75L))
                );
                assertThat(payableRewards1).containsExactlyInAnyOrder(
                        new PayableReward(rewardId1, usdc.forNetwork(Network.ETHEREUM), PositiveAmount.of(75L))
                );
                assertThat(payableRewards2).containsExactlyInAnyOrder(
                        new PayableReward(rewardId2, usdc.forNetwork(Network.ETHEREUM), PositiveAmount.of(75L))
                );
            }
        }

        @Test
        void should_return_payable_rewards_on_multiple_currencies() {
            // Given
            accountingService.fund(unlockedSponsorAccountUsdc1.id(), fakeTransaction(Network.ETHEREUM, PositiveAmount.of(150L)));
            accountingService.fund(unlockedSponsorAccountOp.id(), fakeTransaction(Network.OPTIMISM, PositiveAmount.of(90L)));
            {
                // When
                final var payableRewards = accountingService.getPayableRewards(Set.of(rewardId1, rewardId2, rewardId3, rewardId4, rewardId5, rewardId6));

                // Then
                assertThat(payableRewards).containsExactlyInAnyOrder(
                        new PayableReward(rewardId1, usdc.forNetwork(Network.ETHEREUM), PositiveAmount.of(75L)),
                        new PayableReward(rewardId2, usdc.forNetwork(Network.ETHEREUM), PositiveAmount.of(75L)),
                        new PayableReward(rewardId5, op.forNetwork(Network.OPTIMISM), PositiveAmount.of(90L))
                );
            }
            {
                // When
                final var payableRewards = accountingService.getPayableRewards(Set.of(rewardId1, rewardId2, rewardId3, rewardId4, rewardId5));
                final var payableRewards5 = accountingService.getPayableRewards(Set.of(rewardId4, rewardId5));

                // Then
                assertThat(payableRewards).containsExactlyInAnyOrder(
                        new PayableReward(rewardId1, usdc.forNetwork(Network.ETHEREUM), PositiveAmount.of(75L)),
                        new PayableReward(rewardId2, usdc.forNetwork(Network.ETHEREUM), PositiveAmount.of(75L)),
                        new PayableReward(rewardId5, op.forNetwork(Network.OPTIMISM), PositiveAmount.of(90L))
                );
                assertThat(payableRewards5).containsExactlyInAnyOrder(
                        new PayableReward(rewardId5, op.forNetwork(Network.OPTIMISM), PositiveAmount.of(90L))
                );
            }
        }

        @Test
        void should_return_payable_rewards_on_multiple_networks() {
            // Given
            accountingService.fund(unlockedSponsorAccountUsdc1.id(), fakeTransaction(Network.ETHEREUM, PositiveAmount.of(200L)));
            accountingService.fund(unlockedSponsorAccountUsdc2.id(), fakeTransaction(Network.OPTIMISM, PositiveAmount.of(50L)));
            {
                // When
                final var payableRewards = accountingService.getPayableRewards(Set.of(rewardId1, rewardId2, rewardId3, rewardId4, rewardId5, rewardId6));

                // Then
                assertThat(payableRewards).containsExactlyInAnyOrder(
                        new PayableReward(rewardId1, usdc.forNetwork(Network.ETHEREUM), PositiveAmount.of(75L)),
                        new PayableReward(rewardId2, usdc.forNetwork(Network.ETHEREUM), PositiveAmount.of(75L)),
                        new PayableReward(rewardId3, usdc.forNetwork(Network.ETHEREUM), PositiveAmount.of(50L)),
                        new PayableReward(rewardId3, usdc.forNetwork(Network.OPTIMISM), PositiveAmount.of(25L))
                );
            }
            {
                // When
                final var payableRewards = accountingService.getPayableRewards(Set.of(rewardId1, rewardId2, rewardId3, rewardId4, rewardId5));
                final var payableRewards3 = accountingService.getPayableRewards(Set.of(rewardId3));

                // Then
                assertThat(payableRewards).containsExactlyInAnyOrder(
                        new PayableReward(rewardId1, usdc.forNetwork(Network.ETHEREUM), PositiveAmount.of(75L)),
                        new PayableReward(rewardId2, usdc.forNetwork(Network.ETHEREUM), PositiveAmount.of(75L)),
                        new PayableReward(rewardId3, usdc.forNetwork(Network.ETHEREUM), PositiveAmount.of(50L)),
                        new PayableReward(rewardId3, usdc.forNetwork(Network.OPTIMISM), PositiveAmount.of(25L))
                );
                assertThat(payableRewards3).containsExactlyInAnyOrder(
                        new PayableReward(rewardId3, usdc.forNetwork(Network.ETHEREUM), PositiveAmount.of(50L)),
                        new PayableReward(rewardId3, usdc.forNetwork(Network.OPTIMISM), PositiveAmount.of(25L))
                );
            }
        }

        @Test
        void should_return_payable_rewards_on_multiple_currencies_on_multiple_networks() {
            // Given
            accountingService.fund(unlockedSponsorAccountUsdc1.id(), fakeTransaction(Network.ETHEREUM, PositiveAmount.of(125L)));
            accountingService.fund(unlockedSponsorAccountUsdc2.id(), fakeTransaction(Network.OPTIMISM, PositiveAmount.of(30L)));
            accountingService.fund(unlockedSponsorAccountOp.id(), fakeTransaction(Network.OPTIMISM, PositiveAmount.of(100L)));
            {
                // When
                final var payableRewards = accountingService.getPayableRewards(Set.of(rewardId1, rewardId2, rewardId3, rewardId4, rewardId5, rewardId6));

                // Then
                // rewardId1 is payable because it is entirely funded on network ETHEREUM
                // rewardId2 is NOT payable because it is NOT entirely funded on network ETHEREUM (we funded 125L but 150L would have been required)
                // rewardId3 is payable because it is entirely funded on networks ETHEREUM and OPTIMISM (50 are coming from the 125 on ETHEREUM and 25 from the
                // 30 on OPTIMISM)
                // rewardId5 is payable because it is entirely funded on network OPTIMISM (for currency OP)
                assertThat(payableRewards).hasSize(4);
                assertThat(payableRewards).containsOnlyOnce(
                        new PayableReward(rewardId3, usdc.forNetwork(Network.ETHEREUM), PositiveAmount.of(50L)),
                        new PayableReward(rewardId3, usdc.forNetwork(Network.OPTIMISM), PositiveAmount.of(25L)),
                        new PayableReward(rewardId5, op.forNetwork(Network.OPTIMISM), PositiveAmount.of(90L))
                );
                assertThat(List.of(
                        new PayableReward(rewardId1, usdc.forNetwork(Network.ETHEREUM), PositiveAmount.of(75L)),
                        new PayableReward(rewardId2, usdc.forNetwork(Network.ETHEREUM), PositiveAmount.of(75L)),
                        new PayableReward(rewardId3, usdc.forNetwork(Network.ETHEREUM), PositiveAmount.of(50L)),
                        new PayableReward(rewardId3, usdc.forNetwork(Network.OPTIMISM), PositiveAmount.of(25L)),
                        new PayableReward(rewardId5, op.forNetwork(Network.OPTIMISM), PositiveAmount.of(90L)))).containsAll(payableRewards);
            }
            {
                // When
                final var payableRewards = accountingService.getPayableRewards(Set.of(rewardId1, rewardId3));

                // Then
                assertThat(payableRewards).hasSize(3);
                assertThat(payableRewards).containsOnlyOnce(
                        new PayableReward(rewardId3, usdc.forNetwork(Network.ETHEREUM), PositiveAmount.of(50L)),
                        new PayableReward(rewardId3, usdc.forNetwork(Network.OPTIMISM), PositiveAmount.of(25L))
                );
                assertThat(List.of(
                        new PayableReward(rewardId1, usdc.forNetwork(Network.ETHEREUM), PositiveAmount.of(75L)),
                        new PayableReward(rewardId3, usdc.forNetwork(Network.ETHEREUM), PositiveAmount.of(50L)),
                        new PayableReward(rewardId3, usdc.forNetwork(Network.OPTIMISM), PositiveAmount.of(25L)))).containsAll(payableRewards);
            }
        }

        @Test
        void should_return_payable_rewards_unless_they_are_locked() {
            // Given
            accountingService.fund(unlockedSponsorAccountUsdc1.id(), fakeTransaction(Network.ETHEREUM, PositiveAmount.of(100_000L)));
            accountingService.fund(unlockedSponsorAccountUsdc2.id(), fakeTransaction(Network.ETHEREUM, PositiveAmount.of(100_000L)));
            accountingService.fund(lockedSponsorAccountUsdc.id(), fakeTransaction(Network.ETHEREUM, PositiveAmount.of(100_000L)));
            accountingService.fund(unlockedSponsorAccountOp.id(), fakeTransaction(Network.OPTIMISM, PositiveAmount.of(100_000L)));
            {
                // When
                final var payableRewards = accountingService.getPayableRewards(Set.of(rewardId1, rewardId2, rewardId3, rewardId4, rewardId5, rewardId6));

                // Then
                assertThat(payableRewards).containsExactlyInAnyOrder(
                        new PayableReward(rewardId1, usdc.forNetwork(Network.ETHEREUM), PositiveAmount.of(75L)),
                        new PayableReward(rewardId2, usdc.forNetwork(Network.ETHEREUM), PositiveAmount.of(75L)),
                        new PayableReward(rewardId3, usdc.forNetwork(Network.ETHEREUM), PositiveAmount.of(75L)),
                        new PayableReward(rewardId4, usdc.forNetwork(Network.ETHEREUM), PositiveAmount.of(75L)),
                        new PayableReward(rewardId5, op.forNetwork(Network.OPTIMISM), PositiveAmount.of(90L))
                );
            }
            {
                // When
                final var payableRewards = accountingService.getPayableRewards(Set.of(rewardId2, rewardId4, rewardId5));

                // Then
                assertThat(payableRewards).containsExactlyInAnyOrder(
                        new PayableReward(rewardId2, usdc.forNetwork(Network.ETHEREUM), PositiveAmount.of(75L)),
                        new PayableReward(rewardId4, usdc.forNetwork(Network.ETHEREUM), PositiveAmount.of(75L)),
                        new PayableReward(rewardId5, op.forNetwork(Network.OPTIMISM), PositiveAmount.of(90L))
                );
            }
        }

        /*
        We don't want to just return a list of payable-rewards.
        We actually want to return a payable-list of rewards, meaning th whole list can be paid in one go, and in any order.
        Hence, in this test, we want to return only one reward (among rewardId1 and rewardId2), because if we were returning both of them,
        the second one wouldn't be payable anymore once the first one has been paid (there is enough funds to pay one reward, not both).
         */
        @Test
        void should_return_payable_rewards_up_to_sponsor_balance() {
            // Given
            accountingService.fund(unlockedSponsorAccountUsdc1.id(), fakeTransaction(Network.ETHEREUM, PositiveAmount.of(75L)));
            {
                // When
                final var payableRewards = accountingService.getPayableRewards(Set.of(rewardId1, rewardId2, rewardId3, rewardId4, rewardId5, rewardId6));

                // Then
                assertThat(payableRewards).hasSize(1);
                assertThat(List.of(
                        new PayableReward(rewardId1, usdc.forNetwork(Network.ETHEREUM), PositiveAmount.of(75L)),
                        new PayableReward(rewardId2, usdc.forNetwork(Network.ETHEREUM), PositiveAmount.of(75L)))
                ).contains(payableRewards.get(0));
            }
            {
                // When
                final var payableRewards1 = accountingService.getPayableRewards(Set.of(rewardId1));
                final var payableRewards2 = accountingService.getPayableRewards(Set.of(rewardId2));

                // Then
                assertThat(payableRewards1).containsExactly(new PayableReward(rewardId1, usdc.forNetwork(Network.ETHEREUM), PositiveAmount.of(75L)));
                assertThat(payableRewards2).containsExactly(new PayableReward(rewardId2, usdc.forNetwork(Network.ETHEREUM), PositiveAmount.of(75L)));
            }
        }

        @Test
        void should_return_partially_paid_payable_rewards() {
            // Given
            accountingService.fund(unlockedSponsorAccountUsdc1.id(), fakeTransaction(Network.ETHEREUM, PositiveAmount.of(50L)));
            verify(accountingObserver).onSponsorAccountBalanceChanged(any());

            reset(accountingObserver);
            accountingService.fund(unlockedSponsorAccountUsdc2.id(), fakeTransaction(Network.OPTIMISM, PositiveAmount.of(25L)));
            verify(accountingObserver).onSponsorAccountBalanceChanged(any());

            assertThat(accountingService.getPayableRewards(Set.of(rewardId1, rewardId2, rewardId3, rewardId4, rewardId5, rewardId6))).containsExactlyInAnyOrder(
                    new PayableReward(rewardId3, usdc.forNetwork(Network.ETHEREUM), PositiveAmount.of(50L)),
                    new PayableReward(rewardId3, usdc.forNetwork(Network.OPTIMISM), PositiveAmount.of(25L))
            );

            reset(accountingObserver);
            final var reference = fakePaymentReference(Network.ETHEREUM);
            accountingService.pay(rewardId3, reference);
            verify(accountingObserver).onSponsorAccountBalanceChanged(any());
            verify(accountingObserver).onPaymentReceived(rewardId3, reference);
            verify(accountingObserver, never()).onRewardPaid(rewardId3);
            {
                // When
                final var payableRewards = accountingService.getPayableRewards(Set.of(rewardId1, rewardId2, rewardId3, rewardId4, rewardId5, rewardId6));

                // Then
                assertThat(payableRewards).containsExactly(new PayableReward(rewardId3, usdc.forNetwork(Network.OPTIMISM), PositiveAmount.of(25L)));
            }
            {
                // When
                final var payableRewards = accountingService.getPayableRewards(Set.of(rewardId1, rewardId3));
                final var payableRewards1 = accountingService.getPayableRewards(Set.of(rewardId1));

                // Then
                assertThat(payableRewards).containsExactly(new PayableReward(rewardId3, usdc.forNetwork(Network.OPTIMISM), PositiveAmount.of(25L)));
                assertThat(payableRewards1).isEmpty();
            }
        }

        @Test
        void should_not_return_rewards_with_no_invoice() {
            // Given
            accountingService.fund(unlockedSponsorAccountUsdc1.id(), fakeTransaction(Network.ETHEREUM, PositiveAmount.of(75L)));
            when(invoiceStoragePort.invoiceOf(rewardId1)).thenReturn(Optional.empty());

            // When
            final var payableRewards = accountingService.getPayableRewards(Set.of(rewardId1));

            // Then
            assertThat(payableRewards).isEmpty();
        }

        @ParameterizedTest
        @EnumSource(value = Invoice.Status.class, names = {"DRAFT", "TO_REVIEW", "REJECTED", "PAID"})
        void should_not_return_rewards_with_invoice_not_approved(Invoice.Status status) {
            // Given
            accountingService.fund(unlockedSponsorAccountUsdc1.id(), fakeTransaction(Network.ETHEREUM, PositiveAmount.of(75L)));
            when(invoiceStoragePort.invoiceOf(rewardId1)).thenReturn(Optional.of(invoice.status(status)));

            // When
            final var payableRewards = accountingService.getPayableRewards(Set.of(rewardId1));

            // Then
            assertThat(payableRewards).isEmpty();
        }
    }

    /*
     * diagram: https://app.diagrams.net/#G1r4LWMljxwrZyAPJkvC1_rxL8Zsj0v1WN
     */
    @Test
    void complete_test_case() {
        // Given
        final var sponsor1 = SponsorId.random();
        final var sponsor2 = SponsorId.random();
        final var projectId1 = ProjectId.random();
        final var projectId2 = ProjectId.random();
        final var rewardId1 = RewardId.random();
        final var rewardId2 = RewardId.random();
        final var currency = Currencies.ETH.withERC20(ERC20Tokens.STARKNET_ETH);

        when(currencyStorage.get(currency.id())).thenReturn(Optional.of(currency));
        when(currencyStorage.all()).thenReturn(Set.of(currency));

        // When
        final var sponsor1Account1 = accountingService.createSponsorAccountWithInitialAllowance(sponsor1, currency.id(), null,
                PositiveAmount.of(10_000L)).account().id();
        accountingService.fund(sponsor1Account1, fakeTransaction(Network.ETHEREUM, PositiveAmount.of(5_000L)));
        accountingService.fund(sponsor1Account1, fakeTransaction(Network.ETHEREUM, PositiveAmount.of(5_000L)));

        final var sponsor2Account1 = accountingService.createSponsorAccountWithInitialBalance(sponsor1, currency.id(), null,
                fakeTransaction(Network.ETHEREUM, PositiveAmount.of(3_000L))).account().id();
        accountingService.increaseAllowance(sponsor2Account1, Amount.of(17_000L));

        final var sponsor2Account2 = accountingService.createSponsorAccountWithInitialBalance(sponsor2, currency.id(), ZonedDateTime.now().plusMonths(1),
                fakeTransaction(Network.STARKNET, PositiveAmount.of(50_000L))).account().id();
        accountingService.increaseAllowance(sponsor2Account2, Amount.of(-30_000L));

        // Then
        assertAccount(sponsor1Account1, 10_000L, 10_000L, 10_000L);
        assertAccount(sponsor2Account1, 20_000L, 3_000L, 3_000L);
        assertAccount(sponsor2Account2, 20_000L, 50_000L, 0L);

        // When
        accountingService.allocate(sponsor1Account1, projectId1, PositiveAmount.of(8_000L), currency.id());
        accountingService.allocate(sponsor2Account1, projectId1, PositiveAmount.of(15_000L), currency.id());

        accountingService.allocate(sponsor1Account1, projectId2, PositiveAmount.of(1_000L), currency.id());
        accountingService.allocate(sponsor2Account1, projectId2, PositiveAmount.of(2_000L), currency.id());
        accountingService.allocate(sponsor2Account1, projectId2, PositiveAmount.of(3_000L), currency.id());
        accountingService.allocate(sponsor2Account2, projectId2, PositiveAmount.of(5_000L), currency.id());

        // Then
        assertAccount(sponsor1Account1, 1_000L, 10_000L, 10_000L);
        assertAccount(sponsor2Account1, 0L, 3_000L, 3_000L);
        assertAccount(sponsor2Account2, 15_000L, 50_000L, 0L);

        // When
        accountingService.createReward(projectId2, rewardId1, PositiveAmount.of(3_500L), currency.id());
        accountingService.createReward(projectId2, rewardId2, PositiveAmount.of(4_000L), currency.id());

        // Then
        assertThat(accountingService.isPayable(rewardId1, currency.id())).isTrue();
        assertThat(accountingService.isPayable(rewardId2, currency.id())).isFalse();

        // When (special actions to make reward2 payable)
        accountingService.updateSponsorAccount(sponsor2Account2, null);
        accountingService.fund(sponsor2Account1, fakeTransaction(Network.ETHEREUM, PositiveAmount.of(2_500L)));

        // Then
        assertAccount(sponsor1Account1, 1_000L, 10_000L, 10_000L);
        assertAccount(sponsor2Account1, 0L, 5_500L, 5_500L);
        assertAccount(sponsor2Account2, 15_000L, 50_000L, 50_000L);
        assertThat(accountingService.isPayable(rewardId1, currency.id())).isTrue();
        assertThat(accountingService.isPayable(rewardId2, currency.id())).isTrue();

        // When
        final var payments = accountingService.pay(Set.of(rewardId1, rewardId2));

        // Then
        assertThat(payments).hasSize(2);
        final var payment1 = payments.stream().filter(p -> p.network().equals(Network.ETHEREUM)).findFirst().orElseThrow();
        final var payment2 = payments.stream().filter(p -> p.network().equals(Network.STARKNET)).findFirst().orElseThrow();
        assertThat(accountingService.isPayable(rewardId1, currency.id())).isFalse();
        assertThat(accountingService.isPayable(rewardId2, currency.id())).isFalse();

        // When
        final var ethPaymentReference1 = fakePaymentReference(Network.ETHEREUM);
        final var ethPaymentReference2 = fakePaymentReference(Network.ETHEREUM);
        payment1.referenceFor(rewardId1, ethPaymentReference1);
        payment1.referenceFor(rewardId2, ethPaymentReference2);
        accountingService.confirm(payment1);

        // Then
        verify(accountingObserver).onPaymentReceived(rewardId1, ethPaymentReference1);
        verify(accountingObserver).onPaymentReceived(rewardId2, ethPaymentReference2);
        verify(accountingObserver).onRewardPaid(rewardId1);
        verify(accountingObserver, never()).onRewardPaid(rewardId2);

        assertAccount(sponsor1Account1, 1_000L, 9_000L, 9_000L);
        assertAccount(sponsor2Account1, 0L, 500L, 500L);
        assertAccount(sponsor2Account2, 15_000L, 50_000L, 50_000L);

        // When
        reset(accountingObserver);
        final var starknetPaymentReference = fakePaymentReference(Network.STARKNET);
        payment2.referenceFor(rewardId2, starknetPaymentReference);
        accountingService.confirm(payment2);

        // Then
        verify(accountingObserver, never()).onPaymentReceived(eq(rewardId1), any());
        verify(accountingObserver).onPaymentReceived(rewardId2, starknetPaymentReference);
        verify(accountingObserver, never()).onRewardPaid(rewardId1);
        verify(accountingObserver).onRewardPaid(rewardId2);

        assertAccount(sponsor1Account1, 1_000L, 9_000L, 9_000L);
        assertAccount(sponsor2Account1, 0L, 500L, 500L);
        assertAccount(sponsor2Account2, 15_000L, 48_500L, 48_500L);

        assertThat(accountBookEventStorage.events.get(currency).stream().map(IdentifiedAccountBookEvent::data)).containsExactlyInAnyOrder(
                new MintEvent(AccountId.of(sponsor1Account1), PositiveAmount.of(10_000L)),
                new MintEvent(AccountId.of(sponsor2Account1), PositiveAmount.of(3_000L)),
                new MintEvent(AccountId.of(sponsor2Account1), PositiveAmount.of(17_000L)),
                new MintEvent(AccountId.of(sponsor2Account2), PositiveAmount.of(50_000L)),
                new RefundEvent(AccountId.of(sponsor2Account2), AccountId.ROOT, PositiveAmount.of(30_000L)),
                new TransferEvent(AccountId.of(sponsor1Account1), AccountId.of(projectId1), PositiveAmount.of(8_000L)),
                new TransferEvent(AccountId.of(sponsor2Account1), AccountId.of(projectId1), PositiveAmount.of(15_000L)),
                new TransferEvent(AccountId.of(sponsor1Account1), AccountId.of(projectId2), PositiveAmount.of(1_000L)),
                new TransferEvent(AccountId.of(sponsor2Account1), AccountId.of(projectId2), PositiveAmount.of(2_000L)),
                new TransferEvent(AccountId.of(sponsor2Account1), AccountId.of(projectId2), PositiveAmount.of(3_000L)),
                new TransferEvent(AccountId.of(sponsor2Account2), AccountId.of(projectId2), PositiveAmount.of(5_000L)),
                new TransferEvent(AccountId.of(projectId2), AccountId.of(rewardId1), PositiveAmount.of(3_500L)),
                new TransferEvent(AccountId.of(projectId2), AccountId.of(rewardId2), PositiveAmount.of(4_000L)),
                new TransferEvent(AccountId.of(rewardId1), AccountId.of(payment1.id()), PositiveAmount.of(3_500L)),
                new TransferEvent(AccountId.of(rewardId2), AccountId.of(payment1.id()), PositiveAmount.of(2_500L)),
                new TransferEvent(AccountId.of(rewardId2), AccountId.of(payment2.id()), PositiveAmount.of(1_500L)),
                new BurnEvent(AccountId.of(payment1.id()), PositiveAmount.of(6_000L)),
                new BurnEvent(AccountId.of(payment2.id()), PositiveAmount.of(1_500L))
        );

        verify(accountBookObserver, times(18)).on(any());
    }

    private void assertAccount(SponsorAccount.Id sponsorAccountId, Long expectedAllowance, Long expectedBalance, Long expectedUnlockedBalance) {
        final var sponsorAccountStatement = accountingService.getSponsorAccountStatement(sponsorAccountId).orElseThrow();
        assertThat(sponsorAccountStatement.allowance()).isEqualTo(PositiveAmount.of(expectedAllowance));
        assertThat(sponsorAccountStatement.account().balance()).isEqualTo(Amount.of(expectedBalance));
        assertThat(sponsorAccountStatement.account().unlockedBalance()).isEqualTo(Amount.of(expectedUnlockedBalance));
    }
}
