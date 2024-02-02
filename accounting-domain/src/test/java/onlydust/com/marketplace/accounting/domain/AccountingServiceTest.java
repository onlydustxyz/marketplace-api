package onlydust.com.marketplace.accounting.domain;

import com.github.javafaker.Faker;
import onlydust.com.marketplace.accounting.domain.model.*;
import onlydust.com.marketplace.accounting.domain.model.SponsorAccount.Transaction;
import onlydust.com.marketplace.accounting.domain.model.accountbook.AccountBook.AccountId;
import onlydust.com.marketplace.accounting.domain.model.accountbook.AccountBookAggregate.BurnEvent;
import onlydust.com.marketplace.accounting.domain.model.accountbook.AccountBookAggregate.MintEvent;
import onlydust.com.marketplace.accounting.domain.model.accountbook.AccountBookAggregate.RefundEvent;
import onlydust.com.marketplace.accounting.domain.model.accountbook.AccountBookAggregate.TransferEvent;
import onlydust.com.marketplace.accounting.domain.port.out.CurrencyStorage;
import onlydust.com.marketplace.accounting.domain.service.AccountingService;
import onlydust.com.marketplace.accounting.domain.stubs.AccountBookEventStorageStub;
import onlydust.com.marketplace.accounting.domain.stubs.Currencies;
import onlydust.com.marketplace.accounting.domain.stubs.SponsorAccountStorageStub;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AccountingServiceTest {
    final AccountBookEventStorageStub accountBookEventStorage = new AccountBookEventStorageStub();
    final SponsorAccountStorageStub sponsorAccountStorage = new SponsorAccountStorageStub();
    final CurrencyStorage currencyStorage = mock(CurrencyStorage.class);
    final AccountingService accountingService = new AccountingService(accountBookEventStorage, sponsorAccountStorage, currencyStorage);
    final Faker faker = new Faker();

    @Nested
    class GivenAnUnknownCurrency {
        final Currency.Id currencyId = Currency.Id.random();
        final SponsorId sponsorId = SponsorId.random();
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
            assertThatThrownBy(() -> accountingService.transfer(sponsorId, projectId, PositiveAmount.of(10L), currencyId))
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
            assertThatThrownBy(() -> accountingService.refund(sponsorId, projectId, PositiveAmount.of(10L), currencyId))
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
            assertThatThrownBy(() -> accountingService.refund(projectId, SponsorAccount.Id.random(), PositiveAmount.of(10L), currency.id()))
                    // Then
                    .isInstanceOf(OnlyDustException.class)
                    .hasMessageContaining("Cannot refund");

            assertThat(accountBookEventStorage.events).isEmpty();
        }
    }

    private Transaction fakeTransaction(Network network, Amount amount) {
        return Transaction.create(network, faker.random().hex(), amount, faker.rickAndMorty().character(), faker.internet().slug() + ".eth");
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
            accountingService.increaseAllowance(sponsorAccount.id(), amount);
            accountingService.increaseAllowance(sponsorAccount.id(), amount.negate());

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
            accountingService.transfer(sponsorAccount.id(), projectId1, amount, currency.id());
            accountingService.refund(projectId1, sponsorAccount.id(), amount, currency.id());

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
            assertThatThrownBy(() -> accountingService.refund(projectId1, sponsorAccount.id(), PositiveAmount.of(400L), currency.id()))
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
            accountingService.transfer(sponsorAccount.id(), projectId1, PositiveAmount.of(10L), currency.id());
            accountingService.transfer(projectId1, rewardId1, PositiveAmount.of(10L), currency.id());

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
            assertThat(sponsorAccountStorage.get(sponsorAccount.id()).orElseThrow().unlockedBalance()).isEqualTo(Amount.ZERO);
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
            accountingService.transfer(sponsorAccount.id(), projectId1, PositiveAmount.of(70L), currency.id());

            accountingService.transfer(projectId1, rewardId1, PositiveAmount.of(10L), currency.id());
            accountingService.transfer(projectId1, rewardId2, PositiveAmount.of(20L), currency.id());

            accountingService.transfer(projectId1, projectId2, PositiveAmount.of(20L), currency.id());

            accountingService.refund(projectId1, sponsorAccount.id(), PositiveAmount.of(20L), currency.id());

            accountingService.transfer(sponsorAccount.id(), projectId2, PositiveAmount.of(35L), currency.id());

            accountingService.transfer(projectId2, rewardId2, PositiveAmount.of(25L), currency.id());

            assertThat(accountingService.isPayable(rewardId1, currency.id())).isTrue();
            accountingService.pay(rewardId2, currency.id(), fakeTransaction(network, PositiveAmount.of(300L)));

            // Then
            assertThat(accountBookEventStorage.events.get(currency)).contains(
                    new TransferEvent(AccountId.of(sponsorAccount.id()), AccountId.of(projectId1), PositiveAmount.of(70L)),
                    new TransferEvent(AccountId.of(projectId1), AccountId.of(rewardId1), PositiveAmount.of(10L)),
                    new TransferEvent(AccountId.of(projectId1), AccountId.of(rewardId2), PositiveAmount.of(20L)),
                    new TransferEvent(AccountId.of(projectId1), AccountId.of(projectId2), PositiveAmount.of(20L)),
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
            accountingService.transfer(sponsorAccount.id(), projectId2, PositiveAmount.of(100L), currency.id());
            accountingService.transfer(projectId2, rewardId2, PositiveAmount.of(100L), currency.id());

            accountingService.fund(sponsorAccount.id(), fakeTransaction(network, PositiveAmount.of(30L)));
            accountingService.fund(sponsorAccount.id(), fakeTransaction(network, PositiveAmount.of(30L)));
            accountingService.fund(sponsorAccount.id(), fakeTransaction(network, PositiveAmount.of(40L)));

            final var transaction = Transaction.create(Network.ETHEREUM, "0x123456", PositiveAmount.of(1000L), "StarkNet Foundation", "starknet.eth");
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

            accountingService.transfer(sponsorAccount.id(), projectId2, PositiveAmount.of(80L), currency.id());
            accountingService.transfer(projectId2, rewardId1, PositiveAmount.of(40L), currency.id());
            accountingService.transfer(projectId2, rewardId2, PositiveAmount.of(40L), currency.id());

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


        /*
         * Given a sponsor account
         * When I fund it with a given transaction reference
         * Then I cannot fund it again with the same reference
         */
        @Test
        void should_forbid_same_reference_on_same_account() {
            // Given
            final var transaction = fakeTransaction(Network.ETHEREUM, PositiveAmount.of(100L));
            accountingService.fund(sponsorAccount.id(), transaction);

            // When
            assertThatThrownBy(() -> accountingService.fund(sponsorAccount.id(), transaction))
                    // Then
                    .isInstanceOf(OnlyDustException.class)
                    .hasMessageContaining("Transaction with reference %s already exists".formatted(transaction.reference()));
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

        @BeforeEach
        void setup() {
            when(currencyStorage.get(currency.id())).thenReturn(Optional.of(currency));
            sponsorAccount = accountingService.createSponsorAccount(sponsorId, currency.id(), PositiveAmount.of(300L), ZonedDateTime.now().plusDays(1))
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
            accountingService.transfer(sponsorAccount.id(), projectId1, amount, currency.id());
            accountingService.refund(projectId1, sponsorAccount.id(), amount, currency.id());

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
            assertThatThrownBy(() -> accountingService.refund(projectId1, sponsorAccount.id(), PositiveAmount.of(400L), currency.id()))
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
            accountingService.transfer(sponsorAccount.id(), projectId1, PositiveAmount.of(10L), currency.id());
            accountingService.transfer(projectId1, rewardId1, PositiveAmount.of(10L), currency.id());

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
            accountingService.transfer(sponsorAccount.id(), projectId2, PositiveAmount.of(100L), currency.id());
            accountingService.transfer(projectId2, rewardId2, PositiveAmount.of(100L), currency.id());

            accountingService.fund(sponsorAccount.id(), fakeTransaction(network, PositiveAmount.of(30L)));
            accountingService.fund(sponsorAccount.id(), fakeTransaction(network, PositiveAmount.of(30L)));
            accountingService.fund(sponsorAccount.id(), fakeTransaction(network, PositiveAmount.of(40L)));

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

            accountingService.transfer(sponsorAccount.id(), projectId1, amount, currency.id());
            accountingService.transfer(projectId1, rewardId1, amount, currency.id());

            assertThat(accountingService.isPayable(rewardId1, currency.id())).isFalse();
            assertThatThrownBy(() -> accountingService.pay(rewardId1, currency.id(), fakeTransaction(network, amount)))
                    // Then
                    .isInstanceOf(OnlyDustException.class)
                    .hasMessageContaining("Cannot spend from locked account");
        }
    }

    @Nested
    class GivenSeveralSponsorAccounts {
        final Currency currency = Currencies.USDC;
        final SponsorId sponsorId = SponsorId.random();
        SponsorAccount unlockedSponsorSponsorAccount1;
        SponsorAccount unlockedSponsorSponsorAccount2;
        SponsorAccount lockedSponsorSponsorAccount;
        final ProjectId projectId = ProjectId.random();
        final RewardId rewardId = RewardId.random();
        final RewardId rewardId2 = RewardId.random();

        @BeforeEach
        void setup() {
            when(currencyStorage.get(currency.id())).thenReturn(Optional.of(currency));
            unlockedSponsorSponsorAccount1 = accountingService.createSponsorAccount(sponsorId, currency.id(), PositiveAmount.of(100L), null).account();
            unlockedSponsorSponsorAccount2 = accountingService.createSponsorAccount(sponsorId, currency.id(), PositiveAmount.of(100L), null).account();
            lockedSponsorSponsorAccount = accountingService.createSponsorAccount(sponsorId, currency.id(), PositiveAmount.of(100L),
                    ZonedDateTime.now().plusDays(1)).account();
        }

        /*
         * Given 2 sponsor accounts
         * When Only the first account is funded
         * Then The contributor paid by the other account cannot withdraw his money
         */
        @Test
        void should_prevent_contributor_from_withdrawing_if_source_is_not_funded() {
            // Given
            accountingService.transfer(unlockedSponsorSponsorAccount1.id(), projectId, PositiveAmount.of(100L), currency.id());
            accountingService.transfer(unlockedSponsorSponsorAccount2.id(), projectId, PositiveAmount.of(100L), currency.id());
            accountingService.transfer(projectId, rewardId, PositiveAmount.of(200L), currency.id());

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
            accountingService.transfer(unlockedSponsorSponsorAccount1.id(), projectId, PositiveAmount.of(100L), currency.id());
            accountingService.transfer(unlockedSponsorSponsorAccount2.id(), projectId, PositiveAmount.of(100L), currency.id());
            accountingService.transfer(projectId, rewardId, PositiveAmount.of(100L), currency.id());
            accountingService.transfer(projectId, rewardId2, PositiveAmount.of(100L), currency.id());

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
            accountingService.transfer(unlockedSponsorSponsorAccount1.id(), projectId, PositiveAmount.of(100L), currency.id());
            accountingService.transfer(lockedSponsorSponsorAccount.id(), projectId, PositiveAmount.of(100L), currency.id());
            accountingService.transfer(projectId, rewardId, PositiveAmount.of(200L), currency.id());

            accountingService.fund(unlockedSponsorSponsorAccount1.id(), fakeTransaction(Network.ETHEREUM, PositiveAmount.of(100L)));
            accountingService.fund(lockedSponsorSponsorAccount.id(), fakeTransaction(Network.ETHEREUM, PositiveAmount.of(100L)));

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
            accountingService.increaseAllowance(unlockedSponsorSponsorAccount1.id(), PositiveAmount.of(200L));
            accountingService.increaseAllowance(unlockedSponsorSponsorAccount2.id(), PositiveAmount.of(100L));
            accountingService.transfer(unlockedSponsorSponsorAccount1.id(), projectId, PositiveAmount.of(200L), currency.id());
            accountingService.transfer(unlockedSponsorSponsorAccount2.id(), projectId, PositiveAmount.of(100L), currency.id());
            accountingService.transfer(projectId, rewardId, PositiveAmount.of(300L), currency.id());

            accountingService.fund(unlockedSponsorSponsorAccount1.id(), fakeTransaction(Network.ETHEREUM, PositiveAmount.of(200L)));
            accountingService.fund(unlockedSponsorSponsorAccount2.id(), fakeTransaction(Network.OPTIMISM, PositiveAmount.of(100L)));

            assertThat(sponsorAccountStorage.get(unlockedSponsorSponsorAccount1.id()).orElseThrow().unlockedBalance()).isEqualTo(Amount.of(200L));
            assertThat(sponsorAccountStorage.get(unlockedSponsorSponsorAccount2.id()).orElseThrow().unlockedBalance()).isEqualTo(Amount.of(100L));

            // When
            assertThat(accountingService.isPayable(rewardId, currency.id())).isTrue();

            accountingService.pay(rewardId, currency.id(), fakeTransaction(Network.ETHEREUM, PositiveAmount.of(1000L)));
            accountingService.pay(rewardId, currency.id(), fakeTransaction(Network.OPTIMISM, PositiveAmount.of(1000L)));

            // Then
            assertThat(sponsorAccountStorage.get(unlockedSponsorSponsorAccount1.id()).orElseThrow().unlockedBalance()).isEqualTo(Amount.ZERO);
            assertThat(sponsorAccountStorage.get(unlockedSponsorSponsorAccount1.id()).orElseThrow().unlockedBalance()).isEqualTo(Amount.ZERO);
            assertThat(sponsorAccountStorage.get(unlockedSponsorSponsorAccount2.id()).orElseThrow().unlockedBalance()).isEqualTo(Amount.ZERO);
        }
    }
}
