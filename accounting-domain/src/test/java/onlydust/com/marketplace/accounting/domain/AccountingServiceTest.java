package onlydust.com.marketplace.accounting.domain;

import onlydust.com.marketplace.accounting.domain.model.*;
import onlydust.com.marketplace.accounting.domain.model.accountbook.AccountBookAggregate;
import onlydust.com.marketplace.accounting.domain.model.accountbook.AccountBookAggregate.MintEvent;
import onlydust.com.marketplace.accounting.domain.port.out.AccountBookStorage;
import onlydust.com.marketplace.accounting.domain.port.out.CurrencyStorage;
import onlydust.com.marketplace.accounting.domain.port.out.LedgerProvider;
import onlydust.com.marketplace.accounting.domain.service.AccountingService;
import onlydust.com.marketplace.accounting.domain.stubs.Currencies;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.assertj.core.api.Java6Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@SuppressWarnings("unchecked")
public class AccountingServiceTest {
    final AccountBookStorage accountBookStorage = mock(AccountBookStorage.class);
    final LedgerProvider<SponsorId> sponsorLedgerProvider = mock(LedgerProvider.class);
    final LedgerProvider<CommitteeId> committeeLedgerProvider = mock(LedgerProvider.class);
    final LedgerProvider<ProjectId> projectLedgerProvider = mock(LedgerProvider.class);
    final LedgerProvider<ContributorId> contributorLedgerProvider = mock(LedgerProvider.class);
    final LedgerProviderProxy ledgerProviderProxy = new LedgerProviderProxy(
            sponsorLedgerProvider, committeeLedgerProvider, projectLedgerProvider, contributorLedgerProvider);
    final CurrencyStorage currencyStorage = mock(CurrencyStorage.class);
    final AccountingService accountingService = new AccountingService(accountBookStorage, ledgerProviderProxy, currencyStorage);

    @BeforeEach
    void setUp() {
        reset(sponsorLedgerProvider, committeeLedgerProvider, projectLedgerProvider, contributorLedgerProvider, currencyStorage, accountBookStorage);
    }

    /*
     * Given a newly created sponsor
     * When I transfer money to OnlyDust
     * Then A new ledger is created for me and the transfer is registered on it
     */
    @Test
    void should_create_account_and_register_transfer() {
        // Given
        final var sponsorId = SponsorId.random();
        final var currency = Currencies.USD;
        final var ledger = new Ledger();
        final var accountBook = AccountBookAggregate.empty();

        when(currencyStorage.get(currency.id())).thenReturn(Optional.of(currency));
        when(accountBookStorage.get(currency)).thenReturn(accountBook);
        when(sponsorLedgerProvider.get(sponsorId, currency)).thenReturn(Optional.empty());
        when(sponsorLedgerProvider.create(sponsorId, currency)).thenReturn(ledger);

        // When
        accountingService.receiveFrom(sponsorId, PositiveAmount.of(10L), currency.id());

        // Then
        assertThat(accountBook.state().balanceOf(ledger.id())).isEqualTo(PositiveAmount.of(10L));

        verify(accountBookStorage).save(accountBook);
    }

    /*
     * Given a sponsor with a ledger
     * When I transfer money to OnlyDust
     * Then The transfer is registered on my ledger
     */
    @Test
    void should_register_transfer() {
        // Given
        final var sponsorId = SponsorId.random();
        final var currency = Currencies.USD;
        final var ledger = new Ledger();
        final var accountBook = AccountBookAggregate.fromEvents(
                new MintEvent(ledger.id(), PositiveAmount.of(100L))
        );

        when(currencyStorage.get(currency.id())).thenReturn(Optional.of(currency));
        when(accountBookStorage.get(currency)).thenReturn(accountBook);
        when(sponsorLedgerProvider.get(sponsorId, currency)).thenReturn(Optional.of(ledger));

        // When
        accountingService.receiveFrom(sponsorId, PositiveAmount.of(10L), currency.id());

        // Then
        assertThat(accountBook.state().balanceOf(ledger.id())).isEqualTo(PositiveAmount.of(110L));

        verify(accountBookStorage).save(accountBook);
    }


    /*
     * Given a sponsor with a ledger
     * When I transfer money to OnlyDust in an unknown currency
     * Then The transfer is rejected
     */
    @Test
    void should_reject_transfer_when_unknown_currency() {
        // Given
        final var sponsorId = SponsorId.random();
        final var currencyId = Currency.Id.random();

        when(currencyStorage.get(currencyId)).thenReturn(Optional.empty());

        // When
        assertThatThrownBy(() -> accountingService.receiveFrom(sponsorId, PositiveAmount.of(10L), currencyId))
                // Then
                .isInstanceOf(OnlyDustException.class)
                .hasMessage("Currency %s not found".formatted(currencyId));

        verify(accountBookStorage, never()).save(any());
    }

    /*
     * Given a sponsor with a ledger
     * When I refund money from OnlyDust
     * Then The refund is registered on my ledger
     */
    @Test
    void should_register_refund() {
        // Given
        final var sponsorId = SponsorId.random();
        final var currency = Currencies.USD;
        final var ledger = new Ledger();
        final var accountBook = AccountBookAggregate.fromEvents(
                new MintEvent(ledger.id(), PositiveAmount.of(100L))
        );

        when(currencyStorage.get(currency.id())).thenReturn(Optional.of(currency));
        when(accountBookStorage.get(currency)).thenReturn(accountBook);
        when(sponsorLedgerProvider.get(sponsorId, currency)).thenReturn(Optional.of(ledger));

        // When
        accountingService.sendTo(sponsorId, PositiveAmount.of(10L), currency.id());

        // Then
        assertThat(accountBook.state().balanceOf(ledger.id())).isEqualTo(PositiveAmount.of(90L));

        verify(accountBookStorage).save(accountBook);
    }

    /*
     * Given a sponsor with no ledger
     * When I refund money from OnlyDust
     * Then The refund is rejected
     */
    @Test
    void should_reject_refund_when_no_account_found() {
        // Given
        final var sponsorId = SponsorId.random();
        final var currency = Currencies.USD;

        when(currencyStorage.get(currency.id())).thenReturn(Optional.of(currency));
        when(accountBookStorage.get(currency)).thenReturn(AccountBookAggregate.empty());
        when(sponsorLedgerProvider.get(sponsorId, currency)).thenReturn(Optional.empty());

        // When
        assertThatThrownBy(() -> accountingService.sendTo(sponsorId, PositiveAmount.of(10L), currency.id()))
                // Then
                .isInstanceOf(OnlyDustException.class)
                .hasMessage("No ledger found for owner %s in currency %s".formatted(sponsorId, currency));

        verify(accountBookStorage, never()).save(any());
    }

    /*
     * Given a sponsor with a ledger
     * When I refund money from OnlyDust in an unknown currency
     * Then The refund is rejected
     */
    @Test
    void should_reject_refund_when_unknown_currency() {
        // Given
        final var sponsorId = SponsorId.random();
        final var currencyId = Currency.Id.random();

        when(currencyStorage.get(currencyId)).thenReturn(Optional.empty());

        // When
        assertThatThrownBy(() -> accountingService.sendTo(sponsorId, PositiveAmount.of(10L), currencyId))
                // Then
                .isInstanceOf(OnlyDustException.class)
                .hasMessage("Currency %s not found".formatted(currencyId));

        verify(accountBookStorage, never()).save(any());
    }

    /*
     * Given a sponsor with a ledger
     * When I refund money from OnlyDust of more than I sent
     * Then The refund is rejected
     */
    @Test
    void should_reject_refund_when_not_enough_received() {
        // Given
        final var sponsorId = SponsorId.random();
        final var currency = Currencies.USD;
        final var ledger = new Ledger();

        when(currencyStorage.get(currency.id())).thenReturn(Optional.of(currency));
        when(accountBookStorage.get(currency)).thenReturn(AccountBookAggregate.fromEvents(
                new MintEvent(ledger.id(), PositiveAmount.of(100L))
        ));
        when(sponsorLedgerProvider.get(sponsorId, currency)).thenReturn(Optional.of(ledger));

        // When
        assertThatThrownBy(() -> accountingService.sendTo(sponsorId, PositiveAmount.of(110L), currency.id()))
                // Then
                .isInstanceOf(OnlyDustException.class)
                .hasMessageContaining("Cannot transfer");

        verify(accountBookStorage, never()).save(any());
    }

    /*
     * Given a sponsor with a ledger
     * When I allocate money to a committee
     * Then The transfer is registered from my ledger to the committee ledger
     */
    @Test
    void should_register_allocation_to_committee() {
        // Given
        final var currency = Currencies.USD;
        final var sponsorId = SponsorId.random();
        final var sponsorLedger = new Ledger();
        final var committeeId = CommitteeId.random();
        final var committeeLedger = new Ledger();
        final var accountBook = AccountBookAggregate.fromEvents(
                new MintEvent(sponsorLedger.id(), PositiveAmount.of(100L)),
                new MintEvent(committeeLedger.id(), PositiveAmount.of(200L))
        );

        when(currencyStorage.get(currency.id())).thenReturn(Optional.of(currency));
        when(accountBookStorage.get(currency)).thenReturn(accountBook);
        when(sponsorLedgerProvider.get(sponsorId, currency)).thenReturn(Optional.of(sponsorLedger));
        when(committeeLedgerProvider.get(committeeId, currency)).thenReturn(Optional.of(committeeLedger));

        // When
        accountingService.transfer(sponsorId, committeeId, PositiveAmount.of(10L), currency.id());

        // Then
        assertThat(accountBook.state().balanceOf(sponsorLedger.id())).isEqualTo(PositiveAmount.of(90L));
        assertThat(accountBook.state().balanceOf(committeeLedger.id())).isEqualTo(PositiveAmount.of(210L));
        assertThat(accountBook.state().transferredAmount(sponsorLedger.id(), committeeLedger.id())).isEqualTo(PositiveAmount.of(10L));

        verify(accountBookStorage).save(accountBook);
    }

    /*
     * Given a sponsor with a ledger
     * When I allocate money to a committee with no ledger
     * Then a ledger is created for the committee and the transfer is registered from my ledger to the committee ledger
     */
    @Test
    void should_create_account_and_register_allocation_to_committee() {
        // Given
        final var currency = Currencies.USD;
        final var sponsorId = SponsorId.random();
        final var sponsorLedger = new Ledger();
        final var committeeId = CommitteeId.random();
        final var committeeLedger = new Ledger();
        final var accountBook = AccountBookAggregate.fromEvents(
                new MintEvent(sponsorLedger.id(), PositiveAmount.of(100L))
        );

        when(currencyStorage.get(currency.id())).thenReturn(Optional.of(currency));
        when(accountBookStorage.get(currency)).thenReturn(accountBook);
        when(sponsorLedgerProvider.get(sponsorId, currency)).thenReturn(Optional.of(sponsorLedger));
        when(committeeLedgerProvider.get(committeeId, currency)).thenReturn(Optional.empty());
        when(committeeLedgerProvider.create(committeeId, currency)).thenReturn(committeeLedger);

        // When
        accountingService.transfer(sponsorId, committeeId, PositiveAmount.of(10L), currency.id());

        // Then
        assertThat(accountBook.state().balanceOf(sponsorLedger.id())).isEqualTo(PositiveAmount.of(90L));
        assertThat(accountBook.state().balanceOf(committeeLedger.id())).isEqualTo(PositiveAmount.of(10L));
        assertThat(accountBook.state().transferredAmount(sponsorLedger.id(), committeeLedger.id())).isEqualTo(PositiveAmount.of(10L));

        verify(accountBookStorage).save(accountBook);
    }

    /*
     * Given a sponsor with a ledger
     * When I allocate money to a committee in an unknown currency
     * Then The allocation is rejected
     */
    @Test
    void should_reject_allocation_when_unknown_currency() {
        // Given
        final var sponsorId = SponsorId.random();
        final var committeeId = CommitteeId.random();
        final var currencyId = Currency.Id.random();

        when(currencyStorage.get(currencyId)).thenReturn(Optional.empty());

        // When
        assertThatThrownBy(() -> accountingService.transfer(sponsorId, committeeId, PositiveAmount.of(10L), currencyId))
                // Then
                .isInstanceOf(OnlyDustException.class)
                .hasMessage("Currency %s not found".formatted(currencyId));

        verify(accountBookStorage, never()).save(any());
    }

    /*
     * Given a sponsor that has allocated money to a committee
     * When I refund money from the committee
     * Then The refund is registered on my ledger
     */
    @Test
    void should_register_refund_from_committee() {
        // Given
        final var currency = Currencies.USD;
        final var sponsorId = SponsorId.random();
        final var sponsorLedger = new Ledger();
        final var committeeId = CommitteeId.random();
        final var committeeLedger = new Ledger();
        final var accountBook = AccountBookAggregate.fromEvents(
                new MintEvent(sponsorLedger.id(), PositiveAmount.of(100L)),
                new MintEvent(committeeLedger.id(), PositiveAmount.of(200L))
        );

        when(currencyStorage.get(currency.id())).thenReturn(Optional.of(currency));
        when(accountBookStorage.get(currency)).thenReturn(accountBook);
        when(sponsorLedgerProvider.get(sponsorId, currency)).thenReturn(Optional.of(sponsorLedger));
        when(committeeLedgerProvider.get(committeeId, currency)).thenReturn(Optional.of(committeeLedger));

        // When
        accountingService.transfer(sponsorId, committeeId, PositiveAmount.of(10L), currency.id());
        accountingService.refund(committeeId, sponsorId, PositiveAmount.of(5L), currency.id());

        // Then
        assertThat(accountBook.state().balanceOf(sponsorLedger.id())).isEqualTo(PositiveAmount.of(95L));
        assertThat(accountBook.state().balanceOf(committeeLedger.id())).isEqualTo(PositiveAmount.of(205L));
        assertThat(accountBook.state().transferredAmount(sponsorLedger.id(), committeeLedger.id())).isEqualTo(PositiveAmount.of(5L));

        verify(accountBookStorage, times(2)).save(accountBook);
    }

    /*
     * Given a sponsor that has allocated money to a committee
     * When I refund money from the committee in an unknown currency
     * Then The refund is rejected
     */
    @Test
    void should_reject_unallocation_when_unknown_currency() {
        // Given
        final var sponsorId = SponsorId.random();
        final var committeeId = CommitteeId.random();
        final var currencyId = Currency.Id.random();

        when(currencyStorage.get(currencyId)).thenReturn(Optional.empty());

        // When
        assertThatThrownBy(() -> accountingService.refund(sponsorId, committeeId, PositiveAmount.of(10L), currencyId))
                // Then
                .isInstanceOf(OnlyDustException.class)
                .hasMessage("Currency %s not found".formatted(currencyId));

        verify(accountBookStorage, never()).save(any());
    }

    /*
     * Given a sponsor with no ledger
     * When I refund money from a committee
     * Then The refund is rejected
     */
    @Test
    void should_reject_unallocation_when_no_sponsor_account_found() {
        // Given
        final var sponsorId = SponsorId.random();
        final var committeeId = CommitteeId.random();
        final var currency = Currencies.USD;

        when(currencyStorage.get(currency.id())).thenReturn(Optional.of(currency));
        when(accountBookStorage.get(currency)).thenReturn(AccountBookAggregate.empty());
        when(sponsorLedgerProvider.get(sponsorId, currency)).thenReturn(Optional.empty());

        // When
        assertThatThrownBy(() -> accountingService.refund(sponsorId, committeeId, PositiveAmount.of(10L), currency.id()))
                // Then
                .isInstanceOf(OnlyDustException.class)
                .hasMessage("No ledger found for owner %s in currency %s".formatted(sponsorId, currency));

        verify(accountBookStorage, never()).save(any());
    }

    /*
     * Given a sponsor with a ledger
     * When I refund money from a committee with no ledger
     * Then The refund is rejected
     */
    @Test
    void should_reject_unallocation_when_no_committee_account_found() {
        // Given
        final var sponsorId = SponsorId.random();
        final var committeeId = CommitteeId.random();
        final var currency = Currencies.USD;
        final var sponsorLedger = new Ledger();

        when(currencyStorage.get(currency.id())).thenReturn(Optional.of(currency));
        when(accountBookStorage.get(currency)).thenReturn(AccountBookAggregate.fromEvents(
                new MintEvent(sponsorLedger.id(), PositiveAmount.of(100L))
        ));
        when(sponsorLedgerProvider.get(sponsorId, currency)).thenReturn(Optional.of(sponsorLedger));
        when(committeeLedgerProvider.get(committeeId, currency)).thenReturn(Optional.empty());

        // When
        assertThatThrownBy(() -> accountingService.refund(sponsorId, committeeId, PositiveAmount.of(10L), currency.id()))
                // Then
                .isInstanceOf(OnlyDustException.class)
                .hasMessage("No ledger found for owner %s in currency %s".formatted(committeeId, currency));

        verify(accountBookStorage, never()).save(any());
    }

    /*
     * Given a sponsor with a ledger
     * When I refund money from a committee
     * Then The refund is rejected if the sponsor has not allocated enough money
     */
    @Test
    void should_reject_unallocation_when_not_enough_allocated() {
        // Given
        final var currency = Currencies.USD;
        final var sponsorId = SponsorId.random();
        final var sponsorLedger = new Ledger();
        final var committeeId = CommitteeId.random();
        final var committeeLedger = new Ledger();

        when(currencyStorage.get(currency.id())).thenReturn(Optional.of(currency));
        when(accountBookStorage.get(currency)).thenReturn(AccountBookAggregate.fromEvents(
                new MintEvent(sponsorLedger.id(), PositiveAmount.of(100L)),
                new MintEvent(committeeLedger.id(), PositiveAmount.of(200L))
        ));
        when(sponsorLedgerProvider.get(sponsorId, currency)).thenReturn(Optional.of(sponsorLedger));
        when(committeeLedgerProvider.get(committeeId, currency)).thenReturn(Optional.of(committeeLedger));

        // When
        assertThatThrownBy(() -> accountingService.refund(sponsorId, committeeId, PositiveAmount.of(10L), currency.id()))
                // Then
                .isInstanceOf(OnlyDustException.class)
                .hasMessageContaining("Cannot refund");

        verify(accountBookStorage, never()).save(any());
    }

    /*
     * Given a committee with a ledger and a project with a ledger
     * When I fund a project
     * Then The transfer is registered on the project ledger
     */
    @Test
    void should_register_funding() {
        // Given
        final var currency = Currencies.USD;
        final var committeeId = CommitteeId.random();
        final var committeeLedger = new Ledger();
        final var projectId = ProjectId.random();
        final var projectLedger = new Ledger();
        final var accountBook = AccountBookAggregate.fromEvents(
                new MintEvent(committeeLedger.id(), PositiveAmount.of(100L))
        );

        when(currencyStorage.get(currency.id())).thenReturn(Optional.of(currency));

        when(currencyStorage.get(currency.id())).thenReturn(Optional.of(currency));
        when(accountBookStorage.get(currency)).thenReturn(accountBook);
        when(committeeLedgerProvider.get(committeeId, currency)).thenReturn(Optional.of(committeeLedger));
        when(projectLedgerProvider.get(projectId, currency)).thenReturn(Optional.of(projectLedger));

        // When
        accountingService.transfer(committeeId, projectId, PositiveAmount.of(10L), currency.id());

        // Then
        assertThat(accountBook.state().balanceOf(committeeLedger.id())).isEqualTo(PositiveAmount.of(90L));
        assertThat(accountBook.state().balanceOf(projectLedger.id())).isEqualTo(PositiveAmount.of(10L));
        assertThat(accountBook.state().transferredAmount(committeeLedger.id(), projectLedger.id())).isEqualTo(PositiveAmount.of(10L));

        verify(accountBookStorage).save(accountBook);
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
        // Given
        final var currency = Currencies.USD;
        final var sponsorId = SponsorId.random();
        final var sponsorLedger = new Ledger();
        final var committeeId = CommitteeId.random();
        final var committeeLedger = new Ledger();
        final var projectId1 = ProjectId.random();
        final var projectLedger1 = new Ledger();
        final var projectId2 = ProjectId.random();
        final var projectLedger2 = new Ledger();
        final var contributorId1 = ContributorId.random();
        final var contributorLedger1 = new Ledger();
        final var contributorId2 = ContributorId.random();
        final var contributorLedger2 = new Ledger();
        final var accountBook = AccountBookAggregate.fromEvents(
                new MintEvent(sponsorLedger.id(), PositiveAmount.of(100L))
        );

        when(currencyStorage.get(currency.id())).thenReturn(Optional.of(currency));
        when(accountBookStorage.get(currency)).thenReturn(accountBook);
        when(sponsorLedgerProvider.get(sponsorId, currency)).thenReturn(Optional.of(sponsorLedger));
        when(committeeLedgerProvider.get(committeeId, currency)).thenReturn(Optional.of(committeeLedger));
        when(projectLedgerProvider.get(projectId1, currency)).thenReturn(Optional.of(projectLedger1));
        when(projectLedgerProvider.get(projectId2, currency)).thenReturn(Optional.of(projectLedger2));
        when(contributorLedgerProvider.get(contributorId1, currency)).thenReturn(Optional.of(contributorLedger1));
        when(contributorLedgerProvider.get(contributorId2, currency)).thenReturn(Optional.of(contributorLedger2));

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
        assertThat(accountBook.state().balanceOf(sponsorLedger.id())).isEqualTo(PositiveAmount.of(15L));
        assertThat(accountBook.state().balanceOf(committeeLedger.id())).isEqualTo(PositiveAmount.of(0L));
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
