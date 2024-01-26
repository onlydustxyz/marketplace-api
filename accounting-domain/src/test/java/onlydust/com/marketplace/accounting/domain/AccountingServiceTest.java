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
import onlydust.com.marketplace.accounting.domain.stubs.LedgerProviderStub;
import onlydust.com.marketplace.accounting.domain.stubs.LedgerStorageStub;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.assertj.core.api.Java6Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

public class AccountingServiceTest {
    final AccountBookStorage accountBookStorage = mock(AccountBookStorage.class);
    final LedgerProvider<SponsorId> sponsorLedgerProvider = new LedgerProviderStub<>();
    final LedgerProvider<CommitteeId> committeeLedgerProvider = new LedgerProviderStub<>();
    final LedgerProvider<ProjectId> projectLedgerProvider = new LedgerProviderStub<>();
    final LedgerProvider<ContributorId> contributorLedgerProvider = new LedgerProviderStub<>();
    final LedgerProviderProxy ledgerProviderProxy = new LedgerProviderProxy(
            sponsorLedgerProvider, committeeLedgerProvider, projectLedgerProvider, contributorLedgerProvider);
    final LedgerStorageStub ledgerStorage = new LedgerStorageStub();
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
            assertThatThrownBy(() -> accountingService.receiveFrom(sponsorId, PositiveAmount.of(10L), currencyId))
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
            assertThatThrownBy(() -> accountingService.sendTo(sponsorId, PositiveAmount.of(10L), currencyId))
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
        final Currency currency = Currencies.USD;
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
            accountingService.receiveFrom(sponsorId, PositiveAmount.of(10L), currency.id());

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
            assertThatThrownBy(() -> accountingService.sendTo(sponsorId, PositiveAmount.of(10L), currency.id()))
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
        final SponsorId sponsorId = SponsorId.random();
        Ledger sponsorLedger = new Ledger();
        final CommitteeId committeeId = CommitteeId.random();
        Ledger committeeLedger = new Ledger();
        final ProjectId projectId1 = ProjectId.random();
        final ProjectId projectId2 = ProjectId.random();
        Ledger projectLedger2 = new Ledger();
        final ContributorId contributorId1 = ContributorId.random();
        final ContributorId contributorId2 = ContributorId.random();
        Ledger contributorLedger2 = new Ledger();
        final Currency currency = Currencies.USD;
        AccountBookAggregate accountBook;

        @BeforeEach
        void setup() {
            when(currencyStorage.get(currency.id())).thenReturn(Optional.of(currency));
            sponsorLedger = sponsorLedgerProvider.create(sponsorId, currency);
            committeeLedger = committeeLedgerProvider.create(committeeId, currency);
            projectLedger2 = projectLedgerProvider.create(projectId2, currency);
            contributorLedger2 = contributorLedgerProvider.create(contributorId2, currency);
            ledgerStorage.save(sponsorLedger);
            ledgerStorage.save(committeeLedger);
            ledgerStorage.save(projectLedger2);
            ledgerStorage.save(contributorLedger2);

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
            accountingService.receiveFrom(sponsorId, PositiveAmount.of(10L), currency.id());

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
            accountingService.sendTo(sponsorId, PositiveAmount.of(10L), currency.id());

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
            assertThatThrownBy(() -> accountingService.sendTo(sponsorId, PositiveAmount.of(110L), currency.id()))
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

            assertThatThrownBy(() -> accountingService.sendTo(contributorId1, PositiveAmount.of(10L), currency.id(), new TransactionReceipt()))
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
            accountingService.transfer(sponsorId, committeeId, PositiveAmount.of(70L), currency.id());
            accountingService.transfer(committeeId, projectId1, PositiveAmount.of(40L), currency.id());

            accountingService.transfer(projectId1, contributorId1, PositiveAmount.of(10L), currency.id());
            accountingService.transfer(projectId1, contributorId2, PositiveAmount.of(20L), currency.id());

            accountingService.refund(projectId1, committeeId, PositiveAmount.of(10L), currency.id());
            accountingService.transfer(committeeId, projectId2, PositiveAmount.of(20L), currency.id());

            accountingService.refund(committeeId, sponsorId, PositiveAmount.of(20L), currency.id());

            accountingService.transfer(sponsorId, projectId2, PositiveAmount.of(35L), currency.id());

            accountingService.transfer(projectId2, contributorId2, PositiveAmount.of(25L), currency.id());

            accountingService.sendTo(contributorId2, PositiveAmount.of(45L), currency.id());

            // Then
            final var projectLedger1 = projectLedgerProvider.get(projectId1, currency).orElseThrow();
            final var contributorLedger1 = contributorLedgerProvider.get(contributorId1, currency).orElseThrow();

            assertThat(accountBook.state().balanceOf(sponsorLedger.id())).isEqualTo(PositiveAmount.of(15L));
            assertThat(accountBook.state().balanceOf(committeeLedger.id())).isEqualTo(PositiveAmount.of(200L));
            assertThat(accountBook.state().balanceOf(projectLedger1.id())).isEqualTo(PositiveAmount.of(0L));
            assertThat(accountBook.state().balanceOf(projectLedger2.id())).isEqualTo(PositiveAmount.of(30L));

            assertThat(accountBook.state().balanceOf(contributorLedger1.id())).isEqualTo(PositiveAmount.of(10L));
            assertThat(accountBook.state().transferredAmount(projectLedger1.id(), contributorLedger1.id())).isEqualTo(PositiveAmount.of(10L));
            assertThat(accountBook.state().transferredAmount(sponsorLedger.id(), contributorLedger1.id())).isEqualTo(PositiveAmount.of(10L));
            assertThat(accountBook.state().transferredAmount(committeeLedger.id(), contributorLedger1.id())).isEqualTo(PositiveAmount.of(10L));

            assertThat(accountBook.state().balanceOf(contributorLedger2.id())).isEqualTo(PositiveAmount.of(0L));
            assertThat(accountBook.state().transferredAmount(projectLedger1.id(), contributorLedger2.id())).isEqualTo(PositiveAmount.of(20L));
            assertThat(accountBook.state().transferredAmount(projectLedger2.id(), contributorLedger2.id())).isEqualTo(PositiveAmount.of(25L));
            assertThat(accountBook.state().transferredAmount(sponsorLedger.id(), contributorLedger2.id())).isEqualTo(PositiveAmount.of(45L));
            assertThat(accountBook.state().transferredAmount(committeeLedger.id(), contributorLedger2.id())).isEqualTo(PositiveAmount.of(40L));

            verify(accountBookStorage, times(10)).save(accountBook);
        }
    }
}
