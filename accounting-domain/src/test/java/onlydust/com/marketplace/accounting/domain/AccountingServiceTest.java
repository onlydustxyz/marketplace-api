package onlydust.com.marketplace.accounting.domain;

import onlydust.com.marketplace.accounting.domain.model.*;
import onlydust.com.marketplace.accounting.domain.model.accountbook.AccountBookAggregate.BurnEvent;
import onlydust.com.marketplace.accounting.domain.model.accountbook.AccountBookAggregate.MintEvent;
import onlydust.com.marketplace.accounting.domain.model.accountbook.AccountBookAggregate.RefundEvent;
import onlydust.com.marketplace.accounting.domain.model.accountbook.AccountBookAggregate.TransferEvent;
import onlydust.com.marketplace.accounting.domain.port.out.CurrencyStorage;
import onlydust.com.marketplace.accounting.domain.port.out.LedgerProvider;
import onlydust.com.marketplace.accounting.domain.service.AccountingService;
import onlydust.com.marketplace.accounting.domain.stubs.AccountBookEventStorageStub;
import onlydust.com.marketplace.accounting.domain.stubs.Currencies;
import onlydust.com.marketplace.accounting.domain.stubs.LedgerStorageStub;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AccountingServiceTest {
    final AccountBookEventStorageStub accountBookEventStorage = new AccountBookEventStorageStub();
    final LedgerProvider<SponsorId> sponsorLedgerProvider = new LedgerStorageStub<>();
    final LedgerProvider<CommitteeId> committeeLedgerProvider = new LedgerStorageStub<>();
    final LedgerProvider<ProjectId> projectLedgerProvider = new LedgerStorageStub<>();
    final LedgerProvider<RewardId> rewardLedgerProvider = new LedgerStorageStub<>();
    final LedgerProviderProxy ledgerProviderProxy = new LedgerProviderProxy(
            sponsorLedgerProvider, committeeLedgerProvider, projectLedgerProvider, rewardLedgerProvider);
    final LedgerStorageStub<Object> ledgerStorage = new LedgerStorageStub<>();
    final CurrencyStorage currencyStorage = mock(CurrencyStorage.class);
    final AccountingService accountingService = new AccountingService(accountBookEventStorage, ledgerProviderProxy, ledgerStorage, currencyStorage);

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
         * Given a sponsor with a ledger
         * When I transfer money to OnlyDust in an unknown currency
         * Then The transfer is rejected
         */
        @Test
        void should_reject_transfer() {
            // When
            assertThatThrownBy(() -> accountingService.mint(sponsorId, PositiveAmount.of(10L), currencyId))
                    // Then
                    .isInstanceOf(OnlyDustException.class)
                    .hasMessage("Currency %s not found".formatted(currencyId));

            assertThat(accountBookEventStorage.events).isEmpty();
        }

        /*
         * Given a sponsor with a ledger
         * When I refund money from OnlyDust in an unknown currency
         * Then The refund is rejected
         */
        @Test
        void should_reject_refund() {
            // When
            assertThatThrownBy(() -> accountingService.burn(sponsorId, PositiveAmount.of(10L), currencyId))
                    // Then
                    .isInstanceOf(OnlyDustException.class)
                    .hasMessage("Currency %s not found".formatted(currencyId));

            assertThat(accountBookEventStorage.events).isEmpty();
        }


        /*
         * Given a sponsor with a ledger
         * When I allocate money to a project in an unknown currency
         * Then The allocation is rejected
         */
        @Test
        void should_reject_allocation() {
            // When
            assertThatThrownBy(() -> accountingService.transfer(sponsorId, projectId, PositiveAmount.of(10L), currencyId))
                    // Then
                    .isInstanceOf(OnlyDustException.class)
                    .hasMessage("Currency %s not found".formatted(currencyId));

            assertThat(accountBookEventStorage.events).isEmpty();
        }

        /*
         * Given a sponsor that has allocated money to a committee
         * When I refund money from the project in an unknown currency
         * Then The refund is rejected
         */
        @Test
        void should_reject_unallocation() {
            // When
            assertThatThrownBy(() -> accountingService.refund(sponsorId, projectId, PositiveAmount.of(10L), currencyId))
                    // Then
                    .isInstanceOf(OnlyDustException.class)
                    .hasMessage("Currency %s not found".formatted(currencyId));

            assertThat(accountBookEventStorage.events).isEmpty();
        }
    }

    @Nested
    class GivenASponsorWithNoLedger {
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
         * Then A new ledger is created and the mint is registered
         */
        @Test
        void should_create_ledger_and_mint_virtual_balance() {
            // Given
            final var amountToMint = PositiveAmount.of(10L);

            // When
            final var ledger = accountingService.createLedger(sponsorId, currency.id(), amountToMint);

            // Then
            assertThat(accountBookEventStorage.events.get(currency)).contains(new MintEvent(ledger.id(), amountToMint));
            assertThat(ledger.unlockedBalance()).isEqualTo(PositiveAmount.ZERO);
            assertThat(ledger.currency()).isEqualTo(currency);
            assertThat(ledger.ownerId()).isEqualTo(sponsorId);
            assertThat(ledger.network()).isEmpty();
        }

        /*
         * Given a newly created sponsor
         * When I allocate money on it and provide a receipt
         * Then A new ledger is created, the mint is registered and the physical balance is updated
         */
        @Test
        void should_create_ledger_and_funds_it() {
            // Given
            final var amount = PositiveAmount.of(10L);
            final var transaction = new Ledger.Transaction(Ledger.Transaction.Id.random(), amount, Network.ETHEREUM, null);

            // When
            final var ledger = accountingService.createLedger(sponsorId, currency.id(), amount, transaction);

            // Then
            assertThat(accountBookEventStorage.events.get(currency)).contains(new MintEvent(ledger.id(), amount));
            assertThat(ledger.unlockedBalance()).isEqualTo(amount);
            assertThat(ledger.currency()).isEqualTo(currency);
            assertThat(ledger.ownerId()).isEqualTo(sponsorId);
            assertThat(ledger.network()).contains(transaction.network());

            final var savedLedger = ledgerStorage.get(ledger.id()).orElseThrow();
            assertThat(savedLedger.id()).isEqualTo(ledger.id());
            assertThat(savedLedger.unlockedBalance()).isEqualTo(ledger.unlockedBalance());
            assertThat(savedLedger.currency()).isEqualTo(ledger.currency());
            assertThat(savedLedger.ownerId()).isEqualTo(ledger.ownerId());
            assertThat(savedLedger.network()).isEqualTo(ledger.network());
        }

        /*
         * Given a sponsor with no ledger
         * When I refund money from OnlyDust
         * Then The refund is rejected
         */
        @Test
        void should_reject_refund_when_no_account_found() {
            // When
            assertThatThrownBy(() -> accountingService.burn(sponsorId, PositiveAmount.of(10L), currency.id()))
                    // Then
                    .isInstanceOf(OnlyDustException.class)
                    .hasMessage("No ledger found for owner %s in currency %s".formatted(sponsorId, currency));

            assertThat(accountBookEventStorage.events).isEmpty();
        }

        /*
         * Given a sponsor with no ledger
         * When I refund money from a project
         * Then The refund is rejected
         */
        @Test
        void should_reject_unallocation_when_no_sponsor_account_found() {
            // When
            assertThatThrownBy(() -> accountingService.refund(sponsorId, projectId, PositiveAmount.of(10L), currency.id()))
                    // Then
                    .isInstanceOf(OnlyDustException.class)
                    .hasMessage("No ledger found for owner %s in currency %s".formatted(sponsorId, currency));

            assertThat(accountBookEventStorage.events).isEmpty();
        }
    }

    @Nested
    class GivenASponsorsWithALedger {
        final Currency currency = Currencies.USDC;
        final Network network = Network.ETHEREUM;
        final SponsorId sponsorId = SponsorId.random();
        final Ledger sponsorLedger = new Ledger(sponsorId, currency);
        final CommitteeId committeeId = CommitteeId.random();
        final Ledger committeeLedger = new Ledger(committeeId, currency);
        final ProjectId projectId1 = ProjectId.random();
        final ProjectId projectId2 = ProjectId.random();
        final Ledger projectLedger2 = new Ledger(projectId2, currency);
        final RewardId rewardId1 = RewardId.random();
        final RewardId rewardId2 = RewardId.random();
        final Ledger rewardLedger2 = new Ledger(rewardId2, currency);

        @BeforeEach
        void setup() {
            when(currencyStorage.get(currency.id())).thenReturn(Optional.of(currency));
            ledgerStorage.save(sponsorLedger, committeeLedger, projectLedger2, rewardLedger2);

            accountBookEventStorage.events.put(currency, List.of(
                    new MintEvent(sponsorLedger.id(), PositiveAmount.of(300L)),
                    new TransferEvent(sponsorLedger.id(), committeeLedger.id(), PositiveAmount.of(200L))
            ));
        }

        /*
         * Given a sponsor with a ledger
         * When I transfer money to OnlyDust
         * Then The transfer is registered on my ledger
         */
        @Test
        void should_register_transfer() {
            // When
            accountingService.mint(sponsorId, PositiveAmount.of(10L), currency.id());

            // Then
            assertThat(accountBookEventStorage.events.get(currency)).contains(new MintEvent(sponsorLedger.id(), PositiveAmount.of(10L)));
        }


        /*
         * Given a sponsor with a ledger
         * When I refund money from OnlyDust
         * Then The refund is registered on my ledger
         */
        @Test
        void should_register_refund() {
            // When
            accountingService.burn(sponsorId, PositiveAmount.of(10L), currency.id());

            // Then
            assertThat(accountBookEventStorage.events.get(currency)).contains(new BurnEvent(sponsorLedger.id(), PositiveAmount.of(10L)));
        }

        /*
         * Given a sponsor with a ledger
         * When I refund money from OnlyDust of more than I sent
         * Then The refund is rejected
         */
        @Test
        void should_reject_refund_when_not_enough_received() {
            // When
            assertThatThrownBy(() -> accountingService.burn(sponsorId, PositiveAmount.of(110L), currency.id()))
                    // Then
                    .isInstanceOf(OnlyDustException.class)
                    .hasMessageContaining("Cannot burn");
        }

        /*
         * Given a sponsor with a ledger
         * When I allocate money to a committee
         * Then The transfer is registered from my ledger to the committee ledger
         */
        @Test
        void should_register_allocation_to_committee() {
            // When
            accountingService.transfer(sponsorId, committeeId, PositiveAmount.of(10L), currency.id());

            // Then
            assertThat(accountBookEventStorage.events.get(currency)).contains(new TransferEvent(sponsorLedger.id(), committeeLedger.id(),
                    PositiveAmount.of(10L)));
        }

        /*
         * Given a sponsor with a ledger
         * When I allocate money to a committee with no ledger
         * Then a ledger is created for the committee and the transfer is registered from my ledger to the committee ledger
         */
        @Test
        void should_create_account_and_register_allocation_to_project() {
            // When
            accountingService.transfer(sponsorId, projectId1, PositiveAmount.of(10L), currency.id());

            // Then
            final var projectLedger1 = projectLedgerProvider.get(projectId1, currency).orElseThrow();
            assertThat(accountBookEventStorage.events.get(currency)).contains(new TransferEvent(sponsorLedger.id(), projectLedger1.id(),
                    PositiveAmount.of(10L)));
        }

        /*
         * Given a sponsor that has allocated money to a committee
         * When I refund money from the committee
         * Then The refund is registered on my ledger
         */
        @Test
        void should_register_refund_from_committee() {
            // When
            accountingService.refund(committeeId, sponsorId, PositiveAmount.of(150L), currency.id());

            // Then
            assertThat(accountBookEventStorage.events.get(currency)).contains(new RefundEvent(committeeLedger.id(), sponsorLedger.id(),
                    PositiveAmount.of(150L)));
        }

        /*
         * Given a sponsor with a ledger
         * When I refund money from a project with no ledger
         * Then The refund is rejected
         */
        @Test
        void should_reject_unallocation_when_no_project_account_found() {
            // When
            assertThatThrownBy(() -> accountingService.refund(sponsorId, projectId1, PositiveAmount.of(10L), currency.id()))
                    // Then
                    .isInstanceOf(OnlyDustException.class)
                    .hasMessage("No ledger found for owner %s in currency %s".formatted(projectId1, currency));
        }

        /*
         * Given a sponsor with a ledger
         * When I refund money from a committee
         * Then The refund is rejected if the sponsor has not allocated enough money
         */
        @Test
        void should_reject_unallocation_when_not_enough_allocated() {
            // When
            assertThatThrownBy(() -> accountingService.refund(sponsorId, committeeId, PositiveAmount.of(10L), currency.id()))
                    // Then
                    .isInstanceOf(OnlyDustException.class)
                    .hasMessageContaining("Cannot refund");
        }

        /*
         * Given a sponsor with a ledger and a project with a ledger
         * When I fund a project
         * Then The transfer is registered on the project ledger
         */
        @Test
        void should_register_funding() {
            // When
            accountingService.transfer(sponsorId, projectId1, PositiveAmount.of(10L), currency.id());

            // Then
            final var projectLedger1 = projectLedgerProvider.get(projectId1, currency).orElseThrow();
            assertThat(accountBookEventStorage.events.get(currency)).contains(new TransferEvent(sponsorLedger.id(), projectLedger1.id(),
                    PositiveAmount.of(10L)));
        }

        /*
         * Given a sponsor, a project and a contributor with a ledger
         * When the contributor is rewarded by the project but the sponsor is not funded (no real money received)
         * Then The contributor cannot withdraw his money
         */
        @Test
        void should_prevent_contributor_from_withdrawing_if_source_is_not_funded() {
            // When
            accountingService.transfer(sponsorId, projectId1, PositiveAmount.of(10L), currency.id());
            accountingService.transfer(projectId1, rewardId1, PositiveAmount.of(10L), currency.id());

            assertThat(accountingService.isPayable(rewardId1, currency.id())).isFalse();
            assertThatThrownBy(() -> accountingService.pay(rewardId1, currency.id(), network))
                    // Then
                    .isInstanceOf(OnlyDustException.class)
                    .hasMessageContaining("Not enough fund");
        }

        /*
         * Given a sponsor with a ledger
         * When I fund money from a project without minting the corresponding amount
         * Then I can withdraw this money
         */
        @Test
        void should_fund_and_withdraw() {
            // When
            accountingService.fund(sponsorId, PositiveAmount.of(10L), currency.id(), network);
            // Then
            assertThat(sponsorLedgerProvider.get(sponsorId, currency).orElseThrow().unlockedBalance(network)).isEqualTo(PositiveAmount.of(10L));

            // When
            accountingService.withdraw(sponsorId, PositiveAmount.of(10L), currency.id(), network);
            // Then
            assertThat(sponsorLedgerProvider.get(sponsorId, currency).orElseThrow().unlockedBalance(network)).isEqualTo(PositiveAmount.ZERO);
        }

        /*
         * Given a sponsor, a committee, 2 projects and 2 contributors
         * When
         *    - the sponsor funds project 1 via the committee
         *    - project 1 rewards the 2 contributors
         *    - the committee re-allocate unspent funds from project 1 to project 2
         *    - the committee refunds the remaining unspent funds to the sponsor
         *    - the sponsor funds project 2 directly
         *    - project 2 rewards contributor 2
         *    - contributor 2 withdraws his money
         * Then All is well :-)
         */
        @Test
        void should_do_everything() {
            // When
            accountingService.fund(sponsorId, PositiveAmount.of(300L), currency.id(), network);
            accountingService.transfer(sponsorId, committeeId, PositiveAmount.of(70L), currency.id());
            accountingService.transfer(committeeId, projectId1, PositiveAmount.of(40L), currency.id());

            accountingService.transfer(projectId1, rewardId1, PositiveAmount.of(10L), currency.id());
            accountingService.transfer(projectId1, rewardId2, PositiveAmount.of(20L), currency.id());

            accountingService.refund(projectId1, committeeId, PositiveAmount.of(10L), currency.id());
            accountingService.transfer(committeeId, projectId2, PositiveAmount.of(20L), currency.id());

            accountingService.refund(committeeId, sponsorId, PositiveAmount.of(20L), currency.id());

            accountingService.transfer(sponsorId, projectId2, PositiveAmount.of(35L), currency.id());

            accountingService.transfer(projectId2, rewardId2, PositiveAmount.of(25L), currency.id());

            assertThat(accountingService.isPayable(rewardId1, currency.id())).isTrue();
            accountingService.pay(rewardId2, currency.id(), network);

            // Then
            final var projectLedger1 = projectLedgerProvider.get(projectId1, currency).orElseThrow();
            final var rewardLedger1 = rewardLedgerProvider.get(rewardId1, currency).orElseThrow();
            assertThat(accountBookEventStorage.events.get(currency)).contains(
                    new TransferEvent(sponsorLedger.id(), committeeLedger.id(), PositiveAmount.of(70L)),
                    new TransferEvent(committeeLedger.id(), projectLedger1.id(), PositiveAmount.of(40L)),
                    new TransferEvent(projectLedger1.id(), rewardLedger1.id(), PositiveAmount.of(10L)),
                    new TransferEvent(projectLedger1.id(), rewardLedger2.id(), PositiveAmount.of(20L)),
                    new RefundEvent(projectLedger1.id(), committeeLedger.id(), PositiveAmount.of(10L)),
                    new TransferEvent(committeeLedger.id(), projectLedger2.id(), PositiveAmount.of(20L)),
                    new RefundEvent(committeeLedger.id(), sponsorLedger.id(), PositiveAmount.of(20L)),
                    new TransferEvent(sponsorLedger.id(), projectLedger2.id(), PositiveAmount.of(35L)),
                    new TransferEvent(projectLedger2.id(), rewardLedger2.id(), PositiveAmount.of(25L))
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
            accountingService.transfer(sponsorId, projectId2, PositiveAmount.of(100L), currency.id());
            accountingService.transfer(projectId2, rewardId2, PositiveAmount.of(100L), currency.id());

            accountingService.fund(sponsorId, PositiveAmount.of(30L), currency.id(), network);
            accountingService.fund(sponsorId, PositiveAmount.of(30L), currency.id(), network);
            accountingService.fund(sponsorId, PositiveAmount.of(40L), currency.id(), network);

            accountingService.pay(rewardId2, currency.id(), network);

            // Then
            assertThat(accountBookEventStorage.events.get(currency)).contains(
                    new TransferEvent(sponsorLedger.id(), projectLedger2.id(), PositiveAmount.of(100L)),
                    new TransferEvent(projectLedger2.id(), rewardLedger2.id(), PositiveAmount.of(100L))
            );

            assertThat(sponsorLedgerProvider.get(sponsorId, currency).orElseThrow().unlockedBalance(network)).isEqualTo(PositiveAmount.ZERO);
            assertThat(rewardLedgerProvider.get(rewardId2, currency).orElseThrow().unlockedBalance(network)).isEqualTo(PositiveAmount.ZERO);
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
            accountingService.fund(sponsorId, PositiveAmount.of(50L), currency.id(), network);

            accountingService.transfer(sponsorId, projectId2, PositiveAmount.of(80L), currency.id());
            accountingService.transfer(projectId2, rewardId1, PositiveAmount.of(40L), currency.id());
            accountingService.transfer(projectId2, rewardId2, PositiveAmount.of(40L), currency.id());

            assertThat(accountingService.isPayable(rewardId1, currency.id())).isTrue();
            assertThat(accountingService.isPayable(rewardId1, currency.id())).isTrue();
            accountingService.pay(rewardId1, currency.id(), network);

            assertThat(accountingService.isPayable(rewardId2, currency.id())).isFalse();
            assertThatThrownBy(() -> accountingService.pay(rewardId2, currency.id(), network))
                    // Then
                    .isInstanceOf(OnlyDustException.class)
                    .hasMessageContaining("Not enough fund");
        }

        /*
         * Given 1 sponsor that funded its account with locked tokens
         * When A contributor is rewarded by the project
         * Then The contributor cannot withdraw his money
         */
        @Test
        void should_not_withdraw_locked_rewards() {
            // Given
            accountingService.transfer(sponsorId, projectId2, PositiveAmount.of(100L), currency.id());
            accountingService.transfer(projectId2, rewardId2, PositiveAmount.of(100L), currency.id());

            // When
            accountingService.fund(sponsorId, PositiveAmount.of(100L), currency.id(), network, ZonedDateTime.now().plusDays(1));

            assertThat(accountingService.isPayable(rewardId2, currency.id())).isFalse();
            assertThatThrownBy(() -> accountingService.pay(rewardId2, currency.id(), network))
                    // Then
                    .isInstanceOf(OnlyDustException.class)
                    .hasMessageContaining("Not enough fund");
        }


        /*
         * Given 1 sponsor that funded its account with locked tokens
         * When A contributor is rewarded by the project after the unlock date
         * Then The contributor can withdraw his money
         */
        @Test
        void should_withdraw_unlocked_rewards() {
            // Given
            accountingService.transfer(sponsorId, projectId2, PositiveAmount.of(100L), currency.id());
            accountingService.transfer(projectId2, rewardId2, PositiveAmount.of(100L), currency.id());

            // When
            accountingService.fund(sponsorId, PositiveAmount.of(100L), currency.id(), network, ZonedDateTime.now().minusDays(1));
            assertThat(accountingService.isPayable(rewardId2, currency.id())).isTrue();
            accountingService.pay(rewardId2, currency.id(), network);

            // Then
            assertThat(rewardLedger2.unlockedBalance(network)).isEqualTo(PositiveAmount.ZERO);
        }

        /*
         * Given 1 sponsor that funded its account with locked tokens
         * When A contributor is rewarded by the project after the unlock date
         * Then The contributor can withdraw his money only if remaining unlocked balance is enough
         */
        @Test
        void should_forbid_reward_when_unlocked_balance_is_not_enough() {
            // Given
            accountingService.transfer(sponsorId, projectId2, PositiveAmount.of(100L), currency.id());
            accountingService.transfer(projectId2, rewardId1, PositiveAmount.of(50L), currency.id());
            accountingService.transfer(projectId2, rewardId2, PositiveAmount.of(50L), currency.id());

            // When
            accountingService.fund(sponsorId, PositiveAmount.of(50L), currency.id(), network);
            accountingService.fund(sponsorId, PositiveAmount.of(100L), currency.id(), network, ZonedDateTime.now().plusDays(1));

            assertThat(accountingService.isPayable(rewardId1, currency.id())).isTrue();
            assertThat(accountingService.isPayable(rewardId2, currency.id())).isTrue();

            accountingService.pay(rewardId1, currency.id(), network);

            assertThat(accountingService.isPayable(rewardId2, currency.id())).isFalse();
            assertThatThrownBy(() -> accountingService.pay(rewardId2, currency.id(), network))
                    // Then
                    .isInstanceOf(OnlyDustException.class)
                    .hasMessageContaining("Not enough fund");
        }

        /*
         * Given a sponsor that funded its account on Ethereum
         * When A contributor is rewarded by the project
         * Then The contributor cannot withdraw his money on Optimism
         */
        @Test
        void should_withdraw_on_correct_network() {
            // Given
            accountingService.fund(sponsorId, PositiveAmount.of(100L), currency.id(), Network.ETHEREUM);
            accountingService.transfer(sponsorId, projectId2, PositiveAmount.of(100L), currency.id());
            accountingService.transfer(projectId2, rewardId2, PositiveAmount.of(100L), currency.id());

            // When
            assertThat(accountingService.isPayable(rewardId2, currency.id())).isTrue();
            assertThatThrownBy(() -> accountingService.pay(rewardId2, currency.id(), Network.OPTIMISM))
                    // Then
                    .isInstanceOf(OnlyDustException.class)
                    .hasMessage("Not enough fund on ledger %s on network OPTIMISM".formatted(sponsorLedger.id()));
        }
    }

    @Nested
    class Given2SponsorsWithLedgers {
        final Currency currency = Currencies.USDC;
        final Network network = Network.ETHEREUM;
        final SponsorId sponsorId1 = SponsorId.random();
        final Ledger sponsorLedger1 = new Ledger(sponsorId1, currency);
        final SponsorId sponsorId2 = SponsorId.random();
        final Ledger sponsorLedger2 = new Ledger(sponsorId2, currency);
        final ProjectId projectId = ProjectId.random();
        final Ledger projectLedger = new Ledger(projectId, currency);
        final RewardId rewardId = RewardId.random();
        final Ledger rewardLedger = new Ledger(rewardId, currency);
        final RewardId rewardId2 = RewardId.random();
        final Ledger rewardLedger2 = new Ledger(rewardId2, currency);

        @BeforeEach
        void setup() {
            when(currencyStorage.get(currency.id())).thenReturn(Optional.of(currency));
            ledgerStorage.save(sponsorLedger1, sponsorLedger2, projectLedger, rewardLedger, rewardLedger2);
        }

        /*
         * Given 2 sponsors with ledgers
         * When Only sponsor 1 funds its account
         * Then The contributor paid by sponsor 2 cannot withdraw his money
         */
        @Test
        void should_prevent_contributor_from_withdrawing_if_source_is_not_funded() {
            // Given
            accountBookEventStorage.events.put(currency, List.of(
                    new MintEvent(sponsorLedger1.id(), PositiveAmount.of(100L)),
                    new MintEvent(sponsorLedger2.id(), PositiveAmount.of(100L)),
                    new TransferEvent(sponsorLedger2.id(), projectLedger.id(), PositiveAmount.of(100L)),
                    new TransferEvent(projectLedger.id(), rewardLedger.id(), PositiveAmount.of(100L))
            ));

            // When
            accountingService.fund(sponsorId1, PositiveAmount.of(100L), currency.id(), network);

            assertThat(accountingService.isPayable(rewardId, currency.id())).isFalse();
            assertThatThrownBy(() -> accountingService.pay(rewardId, currency.id(), network))
                    // Then
                    .isInstanceOf(OnlyDustException.class)
                    .hasMessageContaining("Not enough fund");
        }


        /*
         * Given 2 sponsors with ledgers that both rewarded a contributor
         * When Only sponsor 1 funds its account
         * Then The contributor can only withdraw the amount funded by sponsor 1
         */
        @Test
        void should_allow_contributor_to_withdraw_only_what_is_funded() {
            // Given
            accountBookEventStorage.events.put(currency, List.of(
                    new MintEvent(sponsorLedger1.id(), PositiveAmount.of(100L)),
                    new MintEvent(sponsorLedger2.id(), PositiveAmount.of(100L)),
                    new TransferEvent(sponsorLedger1.id(), projectLedger.id(), PositiveAmount.of(100L)),
                    new TransferEvent(sponsorLedger2.id(), projectLedger.id(), PositiveAmount.of(100L)),
                    new TransferEvent(projectLedger.id(), rewardLedger.id(), PositiveAmount.of(100L)),
                    new TransferEvent(projectLedger.id(), rewardLedger2.id(), PositiveAmount.of(100L))
            ));

            // When
            accountingService.fund(sponsorId1, PositiveAmount.of(100L), currency.id(), network);

            assertThat(accountingService.isPayable(rewardId, currency.id())).isTrue();
            assertThat(accountingService.isPayable(rewardId2, currency.id())).isFalse();
            accountingService.pay(rewardId, currency.id(), network);

            // Then
            assertThat(accountingService.isPayable(rewardId2, currency.id())).isFalse();
            assertThatThrownBy(() -> accountingService.pay(rewardId2, currency.id(), network))
                    // Then
                    .isInstanceOf(OnlyDustException.class)
                    .hasMessageContaining("Not enough fund");
        }


        /*
         * Given sponsor 1 that funded its account with locked tokens and sponsor 2 that funded its account with unlocked tokens
         * When A contributor is rewarded by the project
         * Then The contributor cannot withdraw his money
         */
        @Test
        void should_spend_first_fundings_even_if_locked() {
            // Given
            accountBookEventStorage.events.put(currency, List.of(
                    new MintEvent(sponsorLedger1.id(), PositiveAmount.of(100L)),
                    new MintEvent(sponsorLedger2.id(), PositiveAmount.of(100L)),
                    new TransferEvent(sponsorLedger1.id(), projectLedger.id(), PositiveAmount.of(100L)),
                    new TransferEvent(sponsorLedger2.id(), projectLedger.id(), PositiveAmount.of(100L)),
                    new TransferEvent(projectLedger.id(), rewardLedger.id(), PositiveAmount.of(100L))
            ));

            accountingService.fund(sponsorId1, PositiveAmount.of(100L), currency.id(), network, ZonedDateTime.now().plusDays(1));
            accountingService.fund(sponsorId1, PositiveAmount.of(100L), currency.id(), network);

            // When
            assertThat(accountingService.isPayable(rewardId, currency.id())).isTrue();
            assertThatThrownBy(() -> accountingService.pay(rewardId, currency.id(), network))
                    // Then
                    .isInstanceOf(OnlyDustException.class)
                    .hasMessageContaining("Not enough fund");
        }


        /*
         * Given sponsor 1 that funded its account with non-locked tokens and sponsor 2 that funded its account with locked tokens
         * When A contributor is rewarded by the project
         * Then The contributor can withdraw non-locked tokens
         */
        @Test
        void should_withdraw_tokens_even_if_some_are_locked_after() {
            // Given
            accountBookEventStorage.events.put(currency, List.of(
                    new MintEvent(sponsorLedger1.id(), PositiveAmount.of(100L)),
                    new MintEvent(sponsorLedger2.id(), PositiveAmount.of(100L)),
                    new TransferEvent(sponsorLedger1.id(), projectLedger.id(), PositiveAmount.of(100L)),
                    new TransferEvent(sponsorLedger2.id(), projectLedger.id(), PositiveAmount.of(100L)),
                    new TransferEvent(projectLedger.id(), rewardLedger.id(), PositiveAmount.of(100L)),
                    new TransferEvent(projectLedger.id(), rewardLedger2.id(), PositiveAmount.of(100L))
            ));

            accountingService.fund(sponsorId1, PositiveAmount.of(100L), currency.id(), network);
            accountingService.fund(sponsorId2, PositiveAmount.of(100L), currency.id(), network, ZonedDateTime.now().plusDays(1));

            // When
            assertThat(accountingService.isPayable(rewardId, currency.id())).isTrue();
            assertThat(accountingService.isPayable(rewardId2, currency.id())).isFalse();
            accountingService.pay(rewardId, currency.id(), network);

            // Then
            assertThat(accountingService.isPayable(rewardId2, currency.id())).isFalse();
            assertThatThrownBy(() -> accountingService.pay(rewardId2, currency.id(), network))
                    // Then
                    .isInstanceOf(OnlyDustException.class)
                    .hasMessageContaining("Not enough fund");
        }

        /*
         * Given sponsor 1 that funded its account on Ethereum and sponsor 2 that funded its account on Optimism
         * When A contributor is rewarded by the project
         * Then The contributor can withdraw his money on both networks
         */
        @Test
        void should_withdraw_on_both_networks() {
            // Given
            accountBookEventStorage.events.put(currency, List.of(
                    new MintEvent(sponsorLedger1.id(), PositiveAmount.of(200L)),
                    new MintEvent(sponsorLedger2.id(), PositiveAmount.of(100L)),
                    new TransferEvent(sponsorLedger1.id(), projectLedger.id(), PositiveAmount.of(200L)),
                    new TransferEvent(sponsorLedger2.id(), projectLedger.id(), PositiveAmount.of(100L)),
                    new TransferEvent(projectLedger.id(), rewardLedger.id(), PositiveAmount.of(300L))
            ));

            accountingService.fund(sponsorId1, PositiveAmount.of(100L), currency.id(), Network.ETHEREUM);
            accountingService.fund(sponsorId1, PositiveAmount.of(100L), currency.id(), Network.OPTIMISM);
            accountingService.fund(sponsorId2, PositiveAmount.of(100L), currency.id(), Network.OPTIMISM);
            assertThat(ledgerStorage.get(sponsorLedger1.id()).orElseThrow().unlockedBalance(Network.ETHEREUM)).isEqualTo(PositiveAmount.of(100L));
            assertThat(ledgerStorage.get(sponsorLedger1.id()).orElseThrow().unlockedBalance(Network.OPTIMISM)).isEqualTo(PositiveAmount.of(100L));
            assertThat(ledgerStorage.get(sponsorLedger2.id()).orElseThrow().unlockedBalance(Network.OPTIMISM)).isEqualTo(PositiveAmount.of(100L));

            // When
            assertThat(accountingService.isPayable(rewardId, currency.id())).isTrue();
            accountingService.pay(rewardId, currency.id(), Network.ETHEREUM);
            accountingService.pay(rewardId, currency.id(), Network.OPTIMISM);

            // Then
            assertThat(ledgerStorage.get(sponsorLedger1.id()).orElseThrow().unlockedBalance(Network.ETHEREUM)).isEqualTo(PositiveAmount.ZERO);
            assertThat(ledgerStorage.get(sponsorLedger1.id()).orElseThrow().unlockedBalance(Network.OPTIMISM)).isEqualTo(PositiveAmount.ZERO);
            assertThat(ledgerStorage.get(sponsorLedger2.id()).orElseThrow().unlockedBalance(Network.OPTIMISM)).isEqualTo(PositiveAmount.ZERO);
        }
    }
}
