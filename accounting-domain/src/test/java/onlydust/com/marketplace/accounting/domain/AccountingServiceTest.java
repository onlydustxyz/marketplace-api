package onlydust.com.marketplace.accounting.domain;

import com.github.javafaker.Faker;
import onlydust.com.marketplace.accounting.domain.model.*;
import onlydust.com.marketplace.accounting.domain.model.SponsorAccount.PaymentReference;
import onlydust.com.marketplace.accounting.domain.model.SponsorAccount.Transaction;
import onlydust.com.marketplace.accounting.domain.model.accountbook.AccountBook.AccountId;
import onlydust.com.marketplace.accounting.domain.model.accountbook.AccountBookAggregate.*;
import onlydust.com.marketplace.accounting.domain.port.out.AccountingObserverPort;
import onlydust.com.marketplace.accounting.domain.port.out.CurrencyStorage;
import onlydust.com.marketplace.accounting.domain.port.out.ProjectAccountingObserver;
import onlydust.com.marketplace.accounting.domain.service.AccountBookFacade;
import onlydust.com.marketplace.accounting.domain.service.AccountingService;
import onlydust.com.marketplace.accounting.domain.stubs.AccountBookEventStorageStub;
import onlydust.com.marketplace.accounting.domain.stubs.Currencies;
import onlydust.com.marketplace.accounting.domain.stubs.ERC20Tokens;
import onlydust.com.marketplace.accounting.domain.stubs.SponsorAccountStorageStub;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

public class AccountingServiceTest {
    final AccountBookEventStorageStub accountBookEventStorage = new AccountBookEventStorageStub();
    final SponsorAccountStorageStub sponsorAccountStorage = new SponsorAccountStorageStub();
    final CurrencyStorage currencyStorage = mock(CurrencyStorage.class);
    final AccountingObserverPort accountingObserver = mock(AccountingObserverPort.class);
    final ProjectAccountingObserver projectAccountingObserver = mock(ProjectAccountingObserver.class);
    final AccountingService accountingService = new AccountingService(accountBookEventStorage, sponsorAccountStorage, currencyStorage, accountingObserver,
            projectAccountingObserver);
    final Faker faker = new Faker();

    @Nested
    class GivenAnUnknownCurrency {
        final Currency.Id currencyId = Currency.Id.random();
        final SponsorAccount.Id sponsorId = SponsorAccount.Id.random();
        final ProjectId projectId = ProjectId.random();

        @BeforeEach
        void setup() {
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
            final var sponsorAccount = accountingService.createSponsorAccount(sponsorId, currency.id(), amountToMint, null);

            // Then
            assertThat(accountBookEventStorage.events.get(currency)).contains(new MintEvent(AccountId.of(sponsorAccount.account().id()), amountToMint));
            assertThat(sponsorAccount.account().unlockedBalance()).isEqualTo(Amount.ZERO);
            assertThat(sponsorAccount.account().currency()).isEqualTo(currency);
            assertThat(sponsorAccount.account().sponsorId()).isEqualTo(sponsorId);
            assertThat(sponsorAccount.account().network()).isEmpty();
            assertThat(sponsorAccount.account().lockedUntil()).isEmpty();
            assertThat(sponsorAccount.allowance()).isEqualTo(amountToMint);
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
            final var sponsorAccount = accountingService.createSponsorAccount(sponsorId, currency.id(), PositiveAmount.of(amount), null, transaction);
            verify(accountingObserver).onSponsorAccountBalanceChanged(sponsorAccount);

            // Then
            assertThat(accountBookEventStorage.events.get(currency)).contains(
                    new MintEvent(AccountId.of(sponsorAccount.account().id()), PositiveAmount.of(amount)));
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
            final var sponsorAccount = accountingService.createSponsorAccount(sponsorId, currency.id(), amount, lockedUntil, transaction);
            verify(accountingObserver).onSponsorAccountBalanceChanged(sponsorAccount);

            // Then
            assertThat(accountBookEventStorage.events.get(currency)).contains(new MintEvent(AccountId.of(sponsorAccount.account().id()), amount));
            assertThat(sponsorAccount.account().unlockedBalance()).isEqualTo(PositiveAmount.ZERO);
            assertThat(sponsorAccount.account().lockedUntil()).contains(lockedUntil.toInstant());

            final var savedAccount = sponsorAccountStorage.get(sponsorAccount.account().id()).orElseThrow();
            assertThat(savedAccount.unlockedBalance()).isEqualTo(PositiveAmount.ZERO);
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
            assertThatThrownBy(() -> accountingService.createSponsorAccount(sponsorId, currency.id(), amount, null, transaction))
                    // Then
                    .isInstanceOf(OnlyDustException.class)
                    .hasMessageContaining("Currency USDC is not supported on network STARKNET");
        }
    }

    private PaymentReference fakePaymentReference(Network network) {
        return new PaymentReference(network, faker.random().hex(), faker.rickAndMorty().character(), faker.internet().slug() + ".eth");
    }

    private Transaction fakeTransaction(Network network, Amount amount) {
        return new Transaction(fakePaymentReference(network), amount);
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

    @Nested
    class GivenASponsorAccount {
        final Currency currency = Currencies.USDC;
        final Network network = Network.ETHEREUM;
        final SponsorId sponsorId = SponsorId.random();
        SponsorAccount sponsorAccount;
        final ProjectId projectId1 = ProjectId.random();
        final ProjectId projectId2 = ProjectId.random();
        final RewardId rewardId1 = RewardId.random();
        final RewardId rewardId2 = RewardId.random();

        @BeforeEach
        void setup() {
            when(currencyStorage.get(currency.id())).thenReturn(Optional.of(currency));
            sponsorAccount = accountingService.createSponsorAccount(sponsorId, currency.id(), PositiveAmount.of(100L), null).account();
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
            assertThat(accountBookEventStorage.events.get(currency)).contains(
                    new MintEvent(AccountId.of(sponsorAccount.id()), amount),
                    new BurnEvent(AccountId.of(sponsorAccount.id()), amount)
            );
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
                    .isInstanceOf(OnlyDustException.class).hasMessageContaining("Cannot burn");
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
            assertThat(accountBookEventStorage.events.get(currency)).contains(
                    new TransferEvent(AccountId.of(sponsorAccount.id()), AccountId.of(projectId1), amount),
                    new RefundEvent(AccountId.of(projectId1), AccountId.of(sponsorAccount.id()), amount)
            );
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
            assertThatThrownBy(() -> accountingService.pay(rewardId1, currency.id(), transaction))
                    // Then
                    .isInstanceOf(OnlyDustException.class)
                    .hasMessage("Sponsor account %s is not funded".formatted(sponsorAccount.id()));
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
         * Given a sponsor, a project, 2 projects and 2 contributors
         * When
         *    - the sponsor funds project 1
         *    - project 1 rewards the 2 contributors
         *    - some project 1 unspent funds are re-allocated to project 2
         *    - the project 1 refunds the remaining unspent funds to the sponsor
         *    - the sponsor funds project 2 directly
         *    - project 2 rewards contributor 2
         *    - contributor 2 withdraws his money
         * Then All is well :-)
         */
        @Test
        void should_do_everything() {
            // When
            accountingService.fund(sponsorAccount.id(), fakeTransaction(network, PositiveAmount.of(300L)));
            verify(accountingObserver).onSponsorAccountBalanceChanged(any());
            accountingService.allocate(sponsorAccount.id(), projectId1, PositiveAmount.of(70L), currency.id());

            accountingService.createReward(projectId1, rewardId1, PositiveAmount.of(10L), currency.id());
            assertOnRewardCreated(rewardId1, true, null, Set.of(network));

            accountingService.createReward(projectId1, rewardId2, PositiveAmount.of(20L), currency.id());
            assertOnRewardCreated(rewardId2, true, null, Set.of(network));

            accountingService.unallocate(projectId1, sponsorAccount.id(), PositiveAmount.of(20L), currency.id());

            accountingService.allocate(sponsorAccount.id(), projectId2, PositiveAmount.of(35L), currency.id());

            accountingService.createReward(projectId2, rewardId2, PositiveAmount.of(25L), currency.id());

            assertThat(accountingService.isPayable(rewardId1, currency.id())).isTrue();
            reset(accountingObserver);
            final var reference = fakePaymentReference(network);
            accountingService.pay(rewardId2, currency.id(), reference);
            verify(accountingObserver).onSponsorAccountBalanceChanged(any());
            verify(accountingObserver).onRewardPaid(rewardId2, reference);

            // Then
            assertThat(accountBookEventStorage.events.get(currency)).contains(
                    new TransferEvent(AccountId.of(sponsorAccount.id()), AccountId.of(projectId1), PositiveAmount.of(70L)),
                    new TransferEvent(AccountId.of(projectId1), AccountId.of(rewardId1), PositiveAmount.of(10L)),
                    new TransferEvent(AccountId.of(projectId1), AccountId.of(rewardId2), PositiveAmount.of(20L)),
                    new RefundEvent(AccountId.of(projectId1), AccountId.of(sponsorAccount.id()), PositiveAmount.of(20L)),
                    new TransferEvent(AccountId.of(sponsorAccount.id()), AccountId.of(projectId2), PositiveAmount.of(35L)),
                    new TransferEvent(AccountId.of(projectId2), AccountId.of(rewardId2), PositiveAmount.of(25L)),
                    new BurnEvent(AccountId.of(rewardId2), PositiveAmount.of(45L))
            );
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

            final var transaction = new Transaction(Network.ETHEREUM, "0x123456", PositiveAmount.of(1000L), "StarkNet Foundation", "starknet.eth");
            accountingService.pay(rewardId2, currency.id(), transaction);

            // Then
            assertThat(accountBookEventStorage.events.get(currency)).contains(
                    new TransferEvent(AccountId.of(sponsorAccount.id()), AccountId.of(projectId2), PositiveAmount.of(100L)),
                    new TransferEvent(AccountId.of(projectId2), AccountId.of(rewardId2), PositiveAmount.of(100L)));

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

            accountingService.pay(rewardId1, currency.id(), fakeTransaction(network, PositiveAmount.of(50L)));

            {
                final var account = accountingService.getSponsorAccountStatement(sponsorAccount.id()).orElseThrow();
                assertThat(account.awaitingPaymentAmount()).isEqualTo(PositiveAmount.of(40L));
                assertThat(account.account().balance()).isEqualTo(Amount.of(10L));
                assertThat(account.allowance()).isEqualTo(PositiveAmount.of(20L));
            }

            assertThat(accountingService.isPayable(rewardId2, currency.id())).isFalse();
            assertThatThrownBy(() -> accountingService.pay(rewardId2, currency.id(), fakeTransaction(network, PositiveAmount.of(50L))))
                    // Then
                    .isInstanceOf(OnlyDustException.class).hasMessageContaining("Not enough fund");
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
            assertThat(accountBookEventStorage.events.get(currency)).contains(
                    new TransferEvent(AccountId.of(sponsorAccount.id()), AccountId.of(projectId1), PositiveAmount.of(100L)),
                    new TransferEvent(AccountId.of(projectId1), AccountId.of(rewardId1), PositiveAmount.of(40L)),
                    new FullRefundEvent(AccountId.of(rewardId1))
            );
        }
    }

    @Nested
    class GivenALockedSponsorAccount {
        final Currency currency = Currencies.USDC;
        final Network network = Network.ETHEREUM;
        final SponsorId sponsorId = SponsorId.random();
        SponsorAccount sponsorAccount;
        final ProjectId projectId1 = ProjectId.random();
        final ProjectId projectId2 = ProjectId.random();
        final RewardId rewardId1 = RewardId.random();
        final RewardId rewardId2 = RewardId.random();
        final ZonedDateTime unlockDate = ZonedDateTime.now().plusDays(1);

        @BeforeEach
        void setup() {
            when(currencyStorage.get(currency.id())).thenReturn(Optional.of(currency));
            sponsorAccount = accountingService.createSponsorAccount(sponsorId, currency.id(), PositiveAmount.of(300L), unlockDate)
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
            assertThat(accountBookEventStorage.events.get(currency)).contains(
                    new MintEvent(AccountId.of(sponsorAccount.id()), amount),
                    new BurnEvent(AccountId.of(sponsorAccount.id()), amount)
            );
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
            assertThat(accountBookEventStorage.events.get(currency)).contains(
                    new TransferEvent(AccountId.of(sponsorAccount.id()), AccountId.of(projectId1), amount),
                    new RefundEvent(AccountId.of(projectId1), AccountId.of(sponsorAccount.id()), amount)
            );
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
            assertThatThrownBy(() -> accountingService.pay(rewardId1, currency.id(), transaction))
                    // Then
                    .isInstanceOf(OnlyDustException.class)
                    .hasMessageContaining("is not funded");
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
            assertThat(accountBookEventStorage.events.get(currency)).contains(
                    new TransferEvent(AccountId.of(sponsorAccount.id()), AccountId.of(projectId2), PositiveAmount.of(100L)),
                    new TransferEvent(AccountId.of(projectId2), AccountId.of(rewardId2), PositiveAmount.of(100L))
            );

            assertThat(sponsorAccountStorage.get(sponsorAccount.id()).orElseThrow().unlockedBalance()).isEqualTo(PositiveAmount.ZERO);
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
            assertThatThrownBy(() -> accountingService.pay(rewardId1, currency.id(), fakeTransaction(network, amount)))
                    // Then
                    .isInstanceOf(OnlyDustException.class)
                    .hasMessageContaining("Cannot spend from locked account");
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
        Currency currency;
        final SponsorId sponsorId = SponsorId.random();
        SponsorAccount unlockedSponsorSponsorAccount1;
        SponsorAccount unlockedSponsorSponsorAccount2;
        SponsorAccount lockedSponsorSponsorAccount;
        final ZonedDateTime unlockDate = ZonedDateTime.now().plusDays(1);
        final ProjectId projectId = ProjectId.random();
        final RewardId rewardId = RewardId.random();
        final RewardId rewardId2 = RewardId.random();

        @BeforeEach
        void setup() {
            currency = Currency.of(ERC20Tokens.ETH_USDC); // build a copy of USDC currency to avoid side effects
            currency.erc20().add(ERC20Tokens.OP_USDC);

            when(currencyStorage.get(currency.id())).thenReturn(Optional.of(currency));
            unlockedSponsorSponsorAccount1 = accountingService.createSponsorAccount(sponsorId, currency.id(), PositiveAmount.of(100L), null).account();
            unlockedSponsorSponsorAccount2 = accountingService.createSponsorAccount(sponsorId, currency.id(), PositiveAmount.of(100L), null).account();
            lockedSponsorSponsorAccount = accountingService.createSponsorAccount(sponsorId, currency.id(), PositiveAmount.of(100L),
                    unlockDate).account();
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
            assertThatThrownBy(() -> accountingService.pay(rewardId, currency.id(), fakeTransaction(Network.ETHEREUM, PositiveAmount.of(100L))))
                    // Then
                    .isInstanceOf(OnlyDustException.class)
                    .hasMessageContaining("is not funded");
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
            accountingService.pay(rewardId, currency.id(), fakeTransaction(Network.ETHEREUM, PositiveAmount.of(1000L)));
            {
                final var account = accountingService.getSponsorAccountStatement(unlockedSponsorSponsorAccount1.id()).orElseThrow();
                assertThat(account.awaitingPaymentAmount()).isEqualTo(PositiveAmount.ZERO);
            }

            // Then
            assertThat(accountingService.isPayable(rewardId2, currency.id())).isFalse();
            assertThatThrownBy(() -> accountingService.pay(rewardId2, currency.id(), fakeTransaction(Network.ETHEREUM, PositiveAmount.of(1000L))))
                    // Then
                    .isInstanceOf(OnlyDustException.class)
                    .hasMessageContaining("is not funded");
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
            assertThatThrownBy(() -> accountingService.pay(rewardId, currency.id(), fakeTransaction(Network.ETHEREUM, PositiveAmount.of(100L))))
                    // Then
                    .isInstanceOf(OnlyDustException.class)
                    .hasMessageContaining("Cannot spend from locked account");
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

            accountingService.pay(rewardId, currency.id(), fakePaymentReference(Network.ETHEREUM));
            accountingService.pay(rewardId, currency.id(), fakePaymentReference(Network.OPTIMISM));

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
            accountingService.pay(rewardId, currency.id(), reference);
            verify(accountingObserver, times(2)).onSponsorAccountBalanceChanged(any());
            verify(accountingObserver).onRewardPaid(rewardId, reference);

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
            assertThat(accountBookEventStorage.events.get(currency)).contains(
                    new TransferEvent(AccountId.of(unlockedSponsorSponsorAccount1.id()), AccountId.of(projectId), PositiveAmount.of(200L)),
                    new TransferEvent(AccountId.of(unlockedSponsorSponsorAccount2.id()), AccountId.of(projectId), PositiveAmount.of(100L)),
                    new TransferEvent(AccountId.of(projectId), AccountId.of(rewardId), PositiveAmount.of(250L)),
                    new FullRefundEvent(AccountId.of(rewardId))
            );
        }
    }

    @Nested
    class GivenAProjectWithBudget {
        final Currency usdc = Currency.of(ERC20Tokens.ETH_USDC); // build a copy of USDC currency to avoid side effects
        final Currency op = Currencies.OP;
        final SponsorId sponsorId = SponsorId.random();
        SponsorAccount unlockedSponsorAccountUsdc1;
        SponsorAccount unlockedSponsorAccountUsdc2;
        SponsorAccount unlockedSponsorAccountOp;
        SponsorAccount lockedSponsorAccountUsdc;
        final ProjectId projectId = ProjectId.random();
        final RewardId rewardId1 = RewardId.random();
        final RewardId rewardId2 = RewardId.random();
        final RewardId rewardId3 = RewardId.random();
        final RewardId rewardId4 = RewardId.random();
        final RewardId rewardId5 = RewardId.random();
        final RewardId rewardId6 = RewardId.random();

        @BeforeEach
        void setup() {
            usdc.erc20().add(ERC20Tokens.OP_USDC);

            when(currencyStorage.get(usdc.id())).thenReturn(Optional.of(usdc));
            when(currencyStorage.get(op.id())).thenReturn(Optional.of(op));
            when(currencyStorage.all()).thenReturn(Set.of(usdc, op));

            final var lockDate = ZonedDateTime.now().plusDays(1);

            unlockedSponsorAccountUsdc1 = accountingService.createSponsorAccount(sponsorId, usdc.id(), PositiveAmount.of(200L), null).account();
            unlockedSponsorAccountUsdc2 = accountingService.createSponsorAccount(sponsorId, usdc.id(), PositiveAmount.of(100L), null).account();
            unlockedSponsorAccountOp = accountingService.createSponsorAccount(sponsorId, op.id(), PositiveAmount.of(100L), null).account();
            lockedSponsorAccountUsdc = accountingService.createSponsorAccount(sponsorId, usdc.id(), PositiveAmount.of(100L),
                    lockDate).account();

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
        void should_return_no_payable_reward_if_no_fund() {
            // When
            final var payableRewards = accountingService.getPayableRewards();

            // Then
            assertThat(payableRewards).isEmpty();
        }

        @Test
        void should_return_payable_rewards_on_one_currency_and_one_network() {
            // Given
            accountingService.fund(unlockedSponsorAccountUsdc1.id(), fakeTransaction(Network.ETHEREUM, PositiveAmount.of(150L)));

            // When
            final var payableRewards = accountingService.getPayableRewards();

            // Then
            assertThat(payableRewards).containsExactlyInAnyOrder(
                    new PayableReward(rewardId1, usdc.forNetwork(Network.ETHEREUM), PositiveAmount.of(75L)),
                    new PayableReward(rewardId2, usdc.forNetwork(Network.ETHEREUM), PositiveAmount.of(75L))
            );
        }

        @Test
        void should_return_payable_rewards_on_multiple_currencies() {
            // Given
            accountingService.fund(unlockedSponsorAccountUsdc1.id(), fakeTransaction(Network.ETHEREUM, PositiveAmount.of(150L)));
            accountingService.fund(unlockedSponsorAccountOp.id(), fakeTransaction(Network.OPTIMISM, PositiveAmount.of(90L)));

            // When
            final var payableRewards = accountingService.getPayableRewards();

            // Then
            assertThat(payableRewards).containsExactlyInAnyOrder(
                    new PayableReward(rewardId1, usdc.forNetwork(Network.ETHEREUM), PositiveAmount.of(75L)),
                    new PayableReward(rewardId2, usdc.forNetwork(Network.ETHEREUM), PositiveAmount.of(75L)),
                    new PayableReward(rewardId5, op.forNetwork(Network.OPTIMISM), PositiveAmount.of(90L))
            );
        }

        @Test
        void should_return_payable_rewards_on_multiple_networks() {
            // Given
            accountingService.fund(unlockedSponsorAccountUsdc1.id(), fakeTransaction(Network.ETHEREUM, PositiveAmount.of(200L)));
            accountingService.fund(unlockedSponsorAccountUsdc2.id(), fakeTransaction(Network.OPTIMISM, PositiveAmount.of(50L)));

            // When
            final var payableRewards = accountingService.getPayableRewards();

            // Then
            assertThat(payableRewards).containsExactlyInAnyOrder(
                    new PayableReward(rewardId1, usdc.forNetwork(Network.ETHEREUM), PositiveAmount.of(75L)),
                    new PayableReward(rewardId2, usdc.forNetwork(Network.ETHEREUM), PositiveAmount.of(75L)),
                    new PayableReward(rewardId3, usdc.forNetwork(Network.ETHEREUM), PositiveAmount.of(50L)),
                    new PayableReward(rewardId3, usdc.forNetwork(Network.OPTIMISM), PositiveAmount.of(25L))
            );
        }

        @Test
        void should_return_payable_rewards_on_multiple_currencies_on_multiple_networks() {
            // Given
            accountingService.fund(unlockedSponsorAccountUsdc1.id(), fakeTransaction(Network.ETHEREUM, PositiveAmount.of(125L)));
            accountingService.fund(unlockedSponsorAccountUsdc2.id(), fakeTransaction(Network.OPTIMISM, PositiveAmount.of(30L)));
            accountingService.fund(unlockedSponsorAccountOp.id(), fakeTransaction(Network.OPTIMISM, PositiveAmount.of(100L)));

            // When
            final var payableRewards = accountingService.getPayableRewards();

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

        @Test
        void should_return_payable_rewards_unless_they_are_locked() {
            // Given
            accountingService.fund(unlockedSponsorAccountUsdc1.id(), fakeTransaction(Network.ETHEREUM, PositiveAmount.of(100_000L)));
            accountingService.fund(unlockedSponsorAccountUsdc2.id(), fakeTransaction(Network.ETHEREUM, PositiveAmount.of(100_000L)));
            accountingService.fund(lockedSponsorAccountUsdc.id(), fakeTransaction(Network.ETHEREUM, PositiveAmount.of(100_000L)));
            accountingService.fund(unlockedSponsorAccountOp.id(), fakeTransaction(Network.OPTIMISM, PositiveAmount.of(100_000L)));

            // When
            final var payableRewards = accountingService.getPayableRewards();

            // Then
            assertThat(payableRewards).containsExactlyInAnyOrder(
                    new PayableReward(rewardId1, usdc.forNetwork(Network.ETHEREUM), PositiveAmount.of(75L)),
                    new PayableReward(rewardId2, usdc.forNetwork(Network.ETHEREUM), PositiveAmount.of(75L)),
                    new PayableReward(rewardId3, usdc.forNetwork(Network.ETHEREUM), PositiveAmount.of(75L)),
                    new PayableReward(rewardId4, usdc.forNetwork(Network.ETHEREUM), PositiveAmount.of(75L)),
                    new PayableReward(rewardId5, op.forNetwork(Network.OPTIMISM), PositiveAmount.of(90L))
            );
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

            // When
            final var payableRewards = accountingService.getPayableRewards();

            // Then
            assertThat(payableRewards).hasSize(1);
            assertThat(List.of(
                    new PayableReward(rewardId1, usdc.forNetwork(Network.ETHEREUM), PositiveAmount.of(75L)),
                    new PayableReward(rewardId2, usdc.forNetwork(Network.ETHEREUM), PositiveAmount.of(75L)))
            ).contains(payableRewards.get(0));
        }

        @Test
        void should_return_partially_paid_payable_rewards() {
            // Given
            accountingService.fund(unlockedSponsorAccountUsdc1.id(), fakeTransaction(Network.ETHEREUM, PositiveAmount.of(50L)));
            verify(accountingObserver).onSponsorAccountBalanceChanged(any());

            reset(accountingObserver);
            accountingService.fund(unlockedSponsorAccountUsdc2.id(), fakeTransaction(Network.OPTIMISM, PositiveAmount.of(25L)));
            verify(accountingObserver).onSponsorAccountBalanceChanged(any());

            assertThat(accountingService.getPayableRewards()).containsExactlyInAnyOrder(
                    new PayableReward(rewardId3, usdc.forNetwork(Network.ETHEREUM), PositiveAmount.of(50L)),
                    new PayableReward(rewardId3, usdc.forNetwork(Network.OPTIMISM), PositiveAmount.of(25L))
            );

            reset(accountingObserver);
            final var reference = fakePaymentReference(Network.ETHEREUM);
            accountingService.pay(rewardId3, usdc.id(), reference);
            verify(accountingObserver).onSponsorAccountBalanceChanged(any());
            verify(accountingObserver).onRewardPaid(rewardId3, reference);

            // When
            final var payableRewards = accountingService.getPayableRewards();

            // Then
            assertThat(payableRewards).containsExactly(new PayableReward(rewardId3, usdc.forNetwork(Network.OPTIMISM), PositiveAmount.of(25L)));
        }


    }
}
