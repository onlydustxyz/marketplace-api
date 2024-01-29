package onlydust.com.marketplace.accounting.domain;

import onlydust.com.marketplace.accounting.domain.model.*;
import onlydust.com.marketplace.accounting.domain.model.accountbook.AccountBookAggregate;
import onlydust.com.marketplace.accounting.domain.model.accountbook.AccountBookAggregate.MintEvent;
import onlydust.com.marketplace.accounting.domain.model.accountbook.AccountBookAggregate.TransferEvent;
import onlydust.com.marketplace.accounting.domain.port.out.AccountBookStorage;
import onlydust.com.marketplace.accounting.domain.port.out.CurrencyStorage;
import onlydust.com.marketplace.accounting.domain.port.out.LedgerProvider;
import onlydust.com.marketplace.accounting.domain.service.AccountingService;
import onlydust.com.marketplace.accounting.domain.stubs.Currencies;
import onlydust.com.marketplace.accounting.domain.stubs.LedgerStorageStub;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;
import java.util.Optional;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.assertj.core.api.Java6Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

public class AccountingServiceTest {
    final AccountBookStorage accountBookStorage = mock(AccountBookStorage.class);
    final LedgerProvider<SponsorId> sponsorLedgerProvider = new LedgerStorageStub<>();
    final LedgerProvider<CommitteeId> committeeLedgerProvider = new LedgerStorageStub<>();
    final LedgerProvider<ProjectId> projectLedgerProvider = new LedgerStorageStub<>();
    final LedgerProvider<ContributorId> contributorLedgerProvider = new LedgerStorageStub<>();
    final LedgerProviderProxy ledgerProviderProxy = new LedgerProviderProxy(
            sponsorLedgerProvider, committeeLedgerProvider, projectLedgerProvider, contributorLedgerProvider);
    final LedgerStorageStub<Object> ledgerStorage = new LedgerStorageStub<>();
    final CurrencyStorage currencyStorage = mock(CurrencyStorage.class);
    final AccountingService accountingService = new AccountingService(accountBookStorage, ledgerProviderProxy, ledgerStorage, currencyStorage);

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

            verify(accountBookStorage, never()).save(any());
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

            verify(accountBookStorage, never()).save(any());
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

            verify(accountBookStorage, never()).save(any());
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

            verify(accountBookStorage, never()).save(any());
        }
    }

    @Nested
    class GivenASponsorWithNoLedger {
        final Currency currency = Currencies.USDC;
        final SponsorId sponsorId = SponsorId.random();
        final ProjectId projectId = ProjectId.random();
        final AccountBookAggregate accountBook = AccountBookAggregate.empty();

        @BeforeEach
        void setup() {
            when(currencyStorage.get(currency.id())).thenReturn(Optional.of(currency));
            when(accountBookStorage.get(currency)).thenReturn(accountBook);
        }

        /*
         * Given a newly created sponsor
         * When I transfer money to OnlyDust
         * Then A new ledger is created for me and the transfer is registered on it
         */
        @Test
        void should_create_ledger_and_register_transfer() {
            // When
            accountingService.mint(sponsorId, PositiveAmount.of(10L), currency.id());

            // Then
            final var sponsorLedger = sponsorLedgerProvider.get(sponsorId, currency).orElseThrow();
            assertThat(accountBook.state().balanceOf(sponsorLedger.id())).isEqualTo(PositiveAmount.of(10L));

            verify(accountBookStorage).save(accountBook);
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

            verify(accountBookStorage, never()).save(any());
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

            verify(accountBookStorage, never()).save(any());
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
        final ContributorId contributorId1 = ContributorId.random();
        final ContributorId contributorId2 = ContributorId.random();
        final Ledger contributorLedger2 = new Ledger(contributorId2, currency);
        AccountBookAggregate accountBook;

        @BeforeEach
        void setup() {
            when(currencyStorage.get(currency.id())).thenReturn(Optional.of(currency));
            ledgerStorage.save(sponsorLedger, committeeLedger, projectLedger2, contributorLedger2);

            accountBook = AccountBookAggregate.fromEvents(
                    new MintEvent(sponsorLedger.id(), PositiveAmount.of(300L)),
                    new TransferEvent(sponsorLedger.id(), committeeLedger.id(), PositiveAmount.of(200L))
            );
            when(accountBookStorage.get(currency)).thenReturn(accountBook);
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
            assertThat(accountBook.state().balanceOf(sponsorLedger.id())).isEqualTo(PositiveAmount.of(110L));

            verify(accountBookStorage).save(accountBook);
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
            assertThat(accountBook.state().balanceOf(sponsorLedger.id())).isEqualTo(PositiveAmount.of(90L));

            verify(accountBookStorage).save(accountBook);
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

            verify(accountBookStorage, never()).save(any());
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
            assertThat(accountBook.state().balanceOf(sponsorLedger.id())).isEqualTo(PositiveAmount.of(90L));
            assertThat(accountBook.state().balanceOf(committeeLedger.id())).isEqualTo(PositiveAmount.of(210L));
            assertThat(accountBook.state().transferredAmount(sponsorLedger.id(), committeeLedger.id())).isEqualTo(PositiveAmount.of(210L));

            verify(accountBookStorage).save(accountBook);
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
            assertThat(accountBook.state().balanceOf(sponsorLedger.id())).isEqualTo(PositiveAmount.of(90L));
            assertThat(accountBook.state().balanceOf(projectLedger1.id())).isEqualTo(PositiveAmount.of(10L));
            assertThat(accountBook.state().transferredAmount(sponsorLedger.id(), projectLedger1.id())).isEqualTo(PositiveAmount.of(10L));

            verify(accountBookStorage).save(accountBook);
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
            assertThat(accountBook.state().balanceOf(sponsorLedger.id())).isEqualTo(PositiveAmount.of(250L));
            assertThat(accountBook.state().balanceOf(committeeLedger.id())).isEqualTo(PositiveAmount.of(50L));
            assertThat(accountBook.state().transferredAmount(sponsorLedger.id(), committeeLedger.id())).isEqualTo(PositiveAmount.of(50L));

            verify(accountBookStorage).save(accountBook);
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

            verify(accountBookStorage, never()).save(any());
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

            verify(accountBookStorage, never()).save(any());
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
            assertThat(accountBook.state().balanceOf(sponsorLedger.id())).isEqualTo(PositiveAmount.of(90L));
            assertThat(accountBook.state().balanceOf(projectLedger1.id())).isEqualTo(PositiveAmount.of(10L));
            assertThat(accountBook.state().transferredAmount(sponsorLedger.id(), projectLedger1.id())).isEqualTo(PositiveAmount.of(10L));

            verify(accountBookStorage).save(accountBook);
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
            accountingService.transfer(projectId1, contributorId1, PositiveAmount.of(10L), currency.id());

            assertThatThrownBy(() -> accountingService.withdraw(contributorId1, PositiveAmount.of(10L), currency.id(), network))
                    // Then
                    .isInstanceOf(OnlyDustException.class)
                    .hasMessageContaining("Not enough fund");
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

            accountingService.transfer(projectId1, contributorId1, PositiveAmount.of(10L), currency.id());
            accountingService.transfer(projectId1, contributorId2, PositiveAmount.of(20L), currency.id());

            accountingService.refund(projectId1, committeeId, PositiveAmount.of(10L), currency.id());
            accountingService.transfer(committeeId, projectId2, PositiveAmount.of(20L), currency.id());

            accountingService.refund(committeeId, sponsorId, PositiveAmount.of(20L), currency.id());

            accountingService.transfer(sponsorId, projectId2, PositiveAmount.of(35L), currency.id());

            accountingService.transfer(projectId2, contributorId2, PositiveAmount.of(25L), currency.id());

            accountingService.withdraw(contributorId2, PositiveAmount.of(45L), currency.id(), network);

            // Then
            final var projectLedger1 = projectLedgerProvider.get(projectId1, currency).orElseThrow();
            final var contributorLedger1 = contributorLedgerProvider.get(contributorId1, currency).orElseThrow();

            assertThat(accountBook.state().balanceOf(sponsorLedger.id())).isEqualTo(PositiveAmount.of(15L));
            assertThat(accountBook.state().balanceOf(committeeLedger.id())).isEqualTo(PositiveAmount.of(200L));
            assertThat(accountBook.state().balanceOf(projectLedger1.id())).isEqualTo(PositiveAmount.ZERO);
            assertThat(accountBook.state().balanceOf(projectLedger2.id())).isEqualTo(PositiveAmount.of(30L));

            assertThat(accountBook.state().balanceOf(contributorLedger1.id())).isEqualTo(PositiveAmount.of(10L));
            assertThat(accountBook.state().transferredAmount(projectLedger1.id(), contributorLedger1.id())).isEqualTo(PositiveAmount.of(10L));
            assertThat(accountBook.state().transferredAmount(sponsorLedger.id(), contributorLedger1.id())).isEqualTo(PositiveAmount.of(10L));
            assertThat(accountBook.state().transferredAmount(committeeLedger.id(), contributorLedger1.id())).isEqualTo(PositiveAmount.of(10L));

            assertThat(accountBook.state().balanceOf(contributorLedger2.id())).isEqualTo(PositiveAmount.ZERO);
            assertThat(accountBook.state().transferredAmount(projectLedger1.id(), contributorLedger2.id())).isEqualTo(PositiveAmount.of(20L));
            assertThat(accountBook.state().transferredAmount(projectLedger2.id(), contributorLedger2.id())).isEqualTo(PositiveAmount.of(25L));
            assertThat(accountBook.state().transferredAmount(sponsorLedger.id(), contributorLedger2.id())).isEqualTo(PositiveAmount.of(45L));
            assertThat(accountBook.state().transferredAmount(committeeLedger.id(), contributorLedger2.id())).isEqualTo(PositiveAmount.of(40L));

            verify(accountBookStorage, times(10)).save(accountBook);
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
            accountingService.transfer(projectId2, contributorId2, PositiveAmount.of(100L), currency.id());

            accountingService.fund(sponsorId, PositiveAmount.of(30L), currency.id(), network);
            accountingService.fund(sponsorId, PositiveAmount.of(30L), currency.id(), network);
            accountingService.fund(sponsorId, PositiveAmount.of(40L), currency.id(), network);

            accountingService.withdraw(contributorId2, PositiveAmount.of(100L), currency.id(), network);

            // Then
            assertThat(accountBook.state().balanceOf(sponsorLedger.id())).isEqualTo(PositiveAmount.ZERO);
            assertThat(accountBook.state().balanceOf(projectLedger2.id())).isEqualTo(PositiveAmount.ZERO);
            assertThat(accountBook.state().balanceOf(contributorLedger2.id())).isEqualTo(PositiveAmount.ZERO);
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
            accountingService.transfer(projectId2, contributorId2, PositiveAmount.of(80L), currency.id());

            accountingService.withdraw(contributorId2, PositiveAmount.of(40L), currency.id(), network);

            assertThatThrownBy(() -> accountingService.withdraw(contributorId2, PositiveAmount.of(40L), currency.id(), network))
                    // Then
                    .isInstanceOf(OnlyDustException.class)
                    .hasMessageContaining("Not enough fund");

            // Then
            assertThat(accountBook.state().balanceOf(sponsorLedger.id())).isEqualTo(PositiveAmount.of(20L));
            assertThat(accountBook.state().balanceOf(projectLedger2.id())).isEqualTo(PositiveAmount.ZERO);
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
            accountingService.transfer(projectId2, contributorId2, PositiveAmount.of(100L), currency.id());

            // When
            accountingService.fund(sponsorId, PositiveAmount.of(100L), currency.id(), network, ZonedDateTime.now().plusDays(1));

            assertThatThrownBy(() -> accountingService.withdraw(contributorId2, PositiveAmount.of(100L), currency.id(), network))
                    // Then
                    .isInstanceOf(OnlyDustException.class)
                    .hasMessageContaining("Cannot spend locked tokens");
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
            accountingService.transfer(projectId2, contributorId2, PositiveAmount.of(100L), currency.id());

            // When
            accountingService.fund(sponsorId, PositiveAmount.of(100L), currency.id(), network, ZonedDateTime.now().minusDays(1));
            accountingService.withdraw(contributorId2, PositiveAmount.of(100L), currency.id(), network);

            // Then
            assertThat(accountBook.state().balanceOf(sponsorLedger.id())).isEqualTo(PositiveAmount.ZERO);
            assertThat(accountBook.state().balanceOf(projectLedger2.id())).isEqualTo(PositiveAmount.ZERO);
            assertThat(accountBook.state().balanceOf(contributorLedger2.id())).isEqualTo(PositiveAmount.ZERO);
            assertThat(contributorLedger2.balance()).isEqualTo(PositiveAmount.ZERO);
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
        final ContributorId contributorId = ContributorId.random();
        final Ledger contributorLedger = new Ledger(contributorId, currency);
        AccountBookAggregate accountBook;

        @BeforeEach
        void setup() {
            when(currencyStorage.get(currency.id())).thenReturn(Optional.of(currency));
            ledgerStorage.save(sponsorLedger1, sponsorLedger2, projectLedger, contributorLedger);
        }

        /*
         * Given 2 sponsors with ledgers
         * When Only sponsor 1 funds its account
         * Then The contributor paid by sponsor 2 cannot withdraw his money
         */
        @Test
        void should_prevent_contributor_from_withdrawing_if_source_is_not_funded() {
            // Given
            accountBook = AccountBookAggregate.fromEvents(
                    new MintEvent(sponsorLedger1.id(), PositiveAmount.of(100L)),
                    new MintEvent(sponsorLedger2.id(), PositiveAmount.of(100L)),
                    new TransferEvent(sponsorLedger2.id(), projectLedger.id(), PositiveAmount.of(100L)),
                    new TransferEvent(projectLedger.id(), contributorLedger.id(), PositiveAmount.of(100L))
            );

            when(accountBookStorage.get(currency)).thenReturn(accountBook);

            // When
            accountingService.fund(sponsorId1, PositiveAmount.of(100L), currency.id(), network);

            assertThatThrownBy(() -> accountingService.withdraw(contributorId, PositiveAmount.of(100L), currency.id(), network))
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
            accountBook = AccountBookAggregate.fromEvents(
                    new MintEvent(sponsorLedger1.id(), PositiveAmount.of(100L)),
                    new MintEvent(sponsorLedger2.id(), PositiveAmount.of(100L)),
                    new TransferEvent(sponsorLedger1.id(), projectLedger.id(), PositiveAmount.of(100L)),
                    new TransferEvent(sponsorLedger2.id(), projectLedger.id(), PositiveAmount.of(100L)),
                    new TransferEvent(projectLedger.id(), contributorLedger.id(), PositiveAmount.of(200L))
            );

            when(accountBookStorage.get(currency)).thenReturn(accountBook);

            // When
            accountingService.fund(sponsorId1, PositiveAmount.of(100L), currency.id(), network);

            accountingService.withdraw(contributorId, PositiveAmount.of(100L), currency.id(), network);

            // Then
            assertThat(accountBook.state().balanceOf(sponsorLedger1.id())).isEqualTo(PositiveAmount.ZERO);
            assertThat(accountBook.state().balanceOf(sponsorLedger2.id())).isEqualTo(PositiveAmount.ZERO);
            assertThat(accountBook.state().balanceOf(projectLedger.id())).isEqualTo(PositiveAmount.ZERO);
            assertThat(accountBook.state().balanceOf(contributorLedger.id())).isEqualTo(PositiveAmount.of(100L));
        }


        /*
         * Given sponsor 1 that funded its account with locked tokens and sponsor 2 that funded its account with unlocked tokens
         * When A contributor is rewarded by the project
         * Then The contributor cannot withdraw his money
         */
        @Test
        void should_spend_first_fundings_even_if_locked() {
            // Given
            accountBook = AccountBookAggregate.fromEvents(
                    new MintEvent(sponsorLedger1.id(), PositiveAmount.of(100L)),
                    new MintEvent(sponsorLedger2.id(), PositiveAmount.of(100L)),
                    new TransferEvent(sponsorLedger1.id(), projectLedger.id(), PositiveAmount.of(100L)),
                    new TransferEvent(sponsorLedger2.id(), projectLedger.id(), PositiveAmount.of(100L)),
                    new TransferEvent(projectLedger.id(), contributorLedger.id(), PositiveAmount.of(100L))
            );

            when(accountBookStorage.get(currency)).thenReturn(accountBook);
            accountingService.fund(sponsorId1, PositiveAmount.of(100L), currency.id(), network, ZonedDateTime.now().plusDays(1));
            accountingService.fund(sponsorId1, PositiveAmount.of(100L), currency.id(), network);

            // When
            assertThatThrownBy(() -> accountingService.withdraw(contributorId, PositiveAmount.of(100L), currency.id(), network))
                    // Then
                    .isInstanceOf(OnlyDustException.class)
                    .hasMessageContaining("Cannot spend locked tokens");
        }
    }
}
