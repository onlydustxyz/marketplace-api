package onlydust.com.marketplace.accounting.domain;

import onlydust.com.marketplace.accounting.domain.model.*;
import onlydust.com.marketplace.accounting.domain.model.accountbook.AccountBookAggregate;
import onlydust.com.marketplace.accounting.domain.model.accountbook.AccountBookAggregate.MintEvent;
import onlydust.com.marketplace.accounting.domain.port.out.AccountBookStorage;
import onlydust.com.marketplace.accounting.domain.port.out.AccountProvider;
import onlydust.com.marketplace.accounting.domain.port.out.CurrencyStorage;
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
    final AccountProvider<SponsorId> sponsorAccountProvider = mock(AccountProvider.class);
    final AccountProvider<CommitteeId> committeeAccountProvider = mock(AccountProvider.class);
    final AccountProvider<ProjectId> projectAccountProvider = mock(AccountProvider.class);
    final AccountProvider<ContributorId> contributorAccountProvider = mock(AccountProvider.class);
    final AccountProviderProxy accountProviderProxy = new AccountProviderProxy(sponsorAccountProvider, committeeAccountProvider, projectAccountProvider,
            contributorAccountProvider);
    final CurrencyStorage currencyStorage = mock(CurrencyStorage.class);
    final AccountingService accountingService = new AccountingService(accountBookStorage, accountProviderProxy, currencyStorage);

    @BeforeEach
    void setUp() {
        reset(sponsorAccountProvider, committeeAccountProvider, projectAccountProvider, contributorAccountProvider, currencyStorage, accountBookStorage);
    }

    /*
     * Given a newly created sponsor
     * When I transfer money to OnlyDust
     * Then A new account is created for me and the transfer is registered on it
     */
    @Test
    void should_create_account_and_register_transfer() {
        // Given
        final var sponsorId = SponsorId.random();
        final var currency = Currencies.USD;
        final var account = AccountId.random();
        final var accountBook = AccountBookAggregate.empty();

        when(currencyStorage.get(currency.id())).thenReturn(Optional.of(currency));
        when(accountBookStorage.get(currency)).thenReturn(accountBook);
        when(sponsorAccountProvider.get(sponsorId, currency)).thenReturn(Optional.empty());
        when(sponsorAccountProvider.create(sponsorId, currency)).thenReturn(account);

        // When
        accountingService.receiveFrom(sponsorId, PositiveAmount.of(10L), currency.id());

        // Then
        assertThat(accountBook.state().balanceOf(account)).isEqualTo(PositiveAmount.of(10L));

        verify(accountBookStorage).save(accountBook);
    }

    /*
     * Given a sponsor with an account
     * When I transfer money to OnlyDust
     * Then The transfer is registered on my account
     */
    @Test
    void should_register_transfer() {
        // Given
        final var sponsorId = SponsorId.random();
        final var currency = Currencies.USD;
        final var account = AccountId.random();
        final var accountBook = AccountBookAggregate.fromEvents(
                new MintEvent(account, PositiveAmount.of(100L))
        );

        when(currencyStorage.get(currency.id())).thenReturn(Optional.of(currency));
        when(accountBookStorage.get(currency)).thenReturn(accountBook);
        when(sponsorAccountProvider.get(sponsorId, currency)).thenReturn(Optional.of(account));

        // When
        accountingService.receiveFrom(sponsorId, PositiveAmount.of(10L), currency.id());

        // Then
        assertThat(accountBook.state().balanceOf(account)).isEqualTo(PositiveAmount.of(110L));

        verify(accountBookStorage).save(accountBook);
    }


    /*
     * Given a sponsor with an account
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
     * Given a sponsor with an account
     * When I refund money from OnlyDust
     * Then The refund is registered on my account
     */
    @Test
    void should_register_refund() {
        // Given
        final var sponsorId = SponsorId.random();
        final var currency = Currencies.USD;
        final var account = AccountId.random();
        final var accountBook = AccountBookAggregate.fromEvents(
                new MintEvent(account, PositiveAmount.of(100L))
        );

        when(currencyStorage.get(currency.id())).thenReturn(Optional.of(currency));
        when(accountBookStorage.get(currency)).thenReturn(accountBook);
        when(sponsorAccountProvider.get(sponsorId, currency)).thenReturn(Optional.of(account));

        // When
        accountingService.refundTo(sponsorId, PositiveAmount.of(10L), currency.id());

        // Then
        assertThat(accountBook.state().balanceOf(account)).isEqualTo(PositiveAmount.of(90L));

        verify(accountBookStorage).save(accountBook);
    }

    /*
     * Given a sponsor with no account
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
        when(sponsorAccountProvider.get(sponsorId, currency)).thenReturn(Optional.empty());

        // When
        assertThatThrownBy(() -> accountingService.refundTo(sponsorId, PositiveAmount.of(10L), currency.id()))
                // Then
                .isInstanceOf(OnlyDustException.class)
                .hasMessage("No account found for owner %s in currency %s".formatted(sponsorId, currency));

        verify(accountBookStorage, never()).save(any());
    }

    /*
     * Given a sponsor with an account
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
        assertThatThrownBy(() -> accountingService.refundTo(sponsorId, PositiveAmount.of(10L), currencyId))
                // Then
                .isInstanceOf(OnlyDustException.class)
                .hasMessage("Currency %s not found".formatted(currencyId));

        verify(accountBookStorage, never()).save(any());
    }

    /*
     * Given a sponsor with an account
     * When I refund money from OnlyDust of more than I sent
     * Then The refund is rejected
     */
    @Test
    void should_reject_refund_when_not_enough_received() {
        // Given
        final var sponsorId = SponsorId.random();
        final var currency = Currencies.USD;
        final var account = AccountId.random();

        when(currencyStorage.get(currency.id())).thenReturn(Optional.of(currency));
        when(accountBookStorage.get(currency)).thenReturn(AccountBookAggregate.fromEvents(
                new MintEvent(account, PositiveAmount.of(100L))
        ));
        when(sponsorAccountProvider.get(sponsorId, currency)).thenReturn(Optional.of(account));

        // When
        assertThatThrownBy(() -> accountingService.refundTo(sponsorId, PositiveAmount.of(110L), currency.id()))
                // Then
                .isInstanceOf(OnlyDustException.class)
                .hasMessageContaining("Cannot refund");

        verify(accountBookStorage, never()).save(any());
    }

    /*
     * Given a sponsor with an account
     * When I allocate money to a committee
     * Then The transfer is registered from my account to the committee account
     */
    @Test
    void should_register_allocation_to_committee() {
        // Given
        final var currency = Currencies.USD;
        final var sponsorId = SponsorId.random();
        final var sponsorAccount = AccountId.random();
        final var committeeId = CommitteeId.random();
        final var committeeAccount = AccountId.random();
        final var accountBook = AccountBookAggregate.fromEvents(
                new MintEvent(sponsorAccount, PositiveAmount.of(100L)),
                new MintEvent(committeeAccount, PositiveAmount.of(200L))
        );

        when(currencyStorage.get(currency.id())).thenReturn(Optional.of(currency));
        when(accountBookStorage.get(currency)).thenReturn(accountBook);
        when(sponsorAccountProvider.get(sponsorId, currency)).thenReturn(Optional.of(sponsorAccount));
        when(committeeAccountProvider.get(committeeId, currency)).thenReturn(Optional.of(committeeAccount));

        // When
        accountingService.transfer(sponsorId, committeeId, PositiveAmount.of(10L), currency.id());

        // Then
        assertThat(accountBook.state().balanceOf(sponsorAccount)).isEqualTo(PositiveAmount.of(90L));
        assertThat(accountBook.state().balanceOf(committeeAccount)).isEqualTo(PositiveAmount.of(210L));
        assertThat(accountBook.state().transferredAmount(sponsorAccount, committeeAccount)).isEqualTo(PositiveAmount.of(10L));

        verify(accountBookStorage).save(accountBook);
    }

    /*
     * Given a sponsor with an account
     * When I allocate money to a committee with no account
     * Then An account is created for the committee and the transfer is registered from my account to the committee account
     */
    @Test
    void should_create_account_and_register_allocation_to_committee() {
        // Given
        final var currency = Currencies.USD;
        final var sponsorId = SponsorId.random();
        final var sponsorAccount = AccountId.random();
        final var committeeId = CommitteeId.random();
        final var committeeAccount = AccountId.random();
        final var accountBook = AccountBookAggregate.fromEvents(
                new MintEvent(sponsorAccount, PositiveAmount.of(100L))
        );

        when(currencyStorage.get(currency.id())).thenReturn(Optional.of(currency));
        when(accountBookStorage.get(currency)).thenReturn(accountBook);
        when(sponsorAccountProvider.get(sponsorId, currency)).thenReturn(Optional.of(sponsorAccount));
        when(committeeAccountProvider.get(committeeId, currency)).thenReturn(Optional.empty());
        when(committeeAccountProvider.create(committeeId, currency)).thenReturn(committeeAccount);

        // When
        accountingService.transfer(sponsorId, committeeId, PositiveAmount.of(10L), currency.id());

        // Then
        assertThat(accountBook.state().balanceOf(sponsorAccount)).isEqualTo(PositiveAmount.of(90L));
        assertThat(accountBook.state().balanceOf(committeeAccount)).isEqualTo(PositiveAmount.of(10L));
        assertThat(accountBook.state().transferredAmount(sponsorAccount, committeeAccount)).isEqualTo(PositiveAmount.of(10L));

        verify(accountBookStorage).save(accountBook);
    }

    /*
     * Given a sponsor with an account
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
     * Then The refund is registered on my account
     */
    @Test
    void should_register_refund_from_committee() {
        // Given
        final var currency = Currencies.USD;
        final var sponsorId = SponsorId.random();
        final var sponsorAccount = AccountId.random();
        final var committeeId = CommitteeId.random();
        final var committeeAccount = AccountId.random();
        final var accountBook = AccountBookAggregate.fromEvents(
                new MintEvent(sponsorAccount, PositiveAmount.of(100L)),
                new MintEvent(committeeAccount, PositiveAmount.of(200L))
        );

        when(currencyStorage.get(currency.id())).thenReturn(Optional.of(currency));
        when(accountBookStorage.get(currency)).thenReturn(accountBook);
        when(sponsorAccountProvider.get(sponsorId, currency)).thenReturn(Optional.of(sponsorAccount));
        when(committeeAccountProvider.get(committeeId, currency)).thenReturn(Optional.of(committeeAccount));

        // When
        accountingService.transfer(sponsorId, committeeId, PositiveAmount.of(10L), currency.id());
        accountingService.refund(committeeId, sponsorId, PositiveAmount.of(5L), currency.id());

        // Then
        assertThat(accountBook.state().balanceOf(sponsorAccount)).isEqualTo(PositiveAmount.of(95L));
        assertThat(accountBook.state().balanceOf(committeeAccount)).isEqualTo(PositiveAmount.of(205L));
        assertThat(accountBook.state().transferredAmount(sponsorAccount, committeeAccount)).isEqualTo(PositiveAmount.of(5L));

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
     * Given a sponsor with no account
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
        when(sponsorAccountProvider.get(sponsorId, currency)).thenReturn(Optional.empty());

        // When
        assertThatThrownBy(() -> accountingService.refund(sponsorId, committeeId, PositiveAmount.of(10L), currency.id()))
                // Then
                .isInstanceOf(OnlyDustException.class)
                .hasMessage("No account found for owner %s in currency %s".formatted(sponsorId, currency));

        verify(accountBookStorage, never()).save(any());
    }

    /*
     * Given a sponsor with an account
     * When I refund money from a committee with no account
     * Then The refund is rejected
     */
    @Test
    void should_reject_unallocation_when_no_committee_account_found() {
        // Given
        final var sponsorId = SponsorId.random();
        final var committeeId = CommitteeId.random();
        final var currency = Currencies.USD;
        final var sponsorAccount = AccountId.random();

        when(currencyStorage.get(currency.id())).thenReturn(Optional.of(currency));
        when(accountBookStorage.get(currency)).thenReturn(AccountBookAggregate.fromEvents(
                new MintEvent(sponsorAccount, PositiveAmount.of(100L))
        ));
        when(sponsorAccountProvider.get(sponsorId, currency)).thenReturn(Optional.of(sponsorAccount));
        when(committeeAccountProvider.get(committeeId, currency)).thenReturn(Optional.empty());

        // When
        assertThatThrownBy(() -> accountingService.refund(sponsorId, committeeId, PositiveAmount.of(10L), currency.id()))
                // Then
                .isInstanceOf(OnlyDustException.class)
                .hasMessage("No account found for owner %s in currency %s".formatted(committeeId, currency));

        verify(accountBookStorage, never()).save(any());
    }

    /*
     * Given a sponsor with an account
     * When I refund money from a committee
     * Then The refund is rejected if the sponsor has not allocated enough money
     */
    @Test
    void should_reject_unallocation_when_not_enough_allocated() {
        // Given
        final var currency = Currencies.USD;
        final var sponsorId = SponsorId.random();
        final var sponsorAccount = AccountId.random();
        final var committeeId = CommitteeId.random();
        final var committeeAccount = AccountId.random();

        when(currencyStorage.get(currency.id())).thenReturn(Optional.of(currency));
        when(accountBookStorage.get(currency)).thenReturn(AccountBookAggregate.fromEvents(
                new MintEvent(sponsorAccount, PositiveAmount.of(100L)),
                new MintEvent(committeeAccount, PositiveAmount.of(200L))
        ));
        when(sponsorAccountProvider.get(sponsorId, currency)).thenReturn(Optional.of(sponsorAccount));
        when(committeeAccountProvider.get(committeeId, currency)).thenReturn(Optional.of(committeeAccount));

        // When
        assertThatThrownBy(() -> accountingService.refund(sponsorId, committeeId, PositiveAmount.of(10L), currency.id()))
                // Then
                .isInstanceOf(OnlyDustException.class)
                .hasMessageContaining("Cannot refund");

        verify(accountBookStorage, never()).save(any());
    }

    /*
     * Given a committee with an account and a project with an account
     * When I fund a project
     * Then The transfer is registered on the project account
     */
    @Test
    void should_register_funding() {
        // Given
        final var currency = Currencies.USD;
        final var committeeId = CommitteeId.random();
        final var committeeAccount = AccountId.random();
        final var projectId = ProjectId.random();
        final var projectAccount = AccountId.random();
        final var accountBook = AccountBookAggregate.fromEvents(
                new MintEvent(committeeAccount, PositiveAmount.of(100L))
        );

        when(currencyStorage.get(currency.id())).thenReturn(Optional.of(currency));

        when(currencyStorage.get(currency.id())).thenReturn(Optional.of(currency));
        when(accountBookStorage.get(currency)).thenReturn(accountBook);
        when(committeeAccountProvider.get(committeeId, currency)).thenReturn(Optional.of(committeeAccount));
        when(projectAccountProvider.get(projectId, currency)).thenReturn(Optional.of(projectAccount));

        // When
        accountingService.transfer(committeeId, projectId, PositiveAmount.of(10L), currency.id());

        // Then
        assertThat(accountBook.state().balanceOf(committeeAccount)).isEqualTo(PositiveAmount.of(90L));
        assertThat(accountBook.state().balanceOf(projectAccount)).isEqualTo(PositiveAmount.of(10L));
        assertThat(accountBook.state().transferredAmount(committeeAccount, projectAccount)).isEqualTo(PositiveAmount.of(10L));

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
     * Then All is well :-)
     */
    @Test
    void should_do_everything() {
        // Given
        final var currency = Currencies.USD;
        final var sponsorId = SponsorId.random();
        final var sponsorAccount = AccountId.random();
        final var committeeId = CommitteeId.random();
        final var committeeAccount = AccountId.random();
        final var projectId1 = ProjectId.random();
        final var projectAccount1 = AccountId.random();
        final var projectId2 = ProjectId.random();
        final var projectAccount2 = AccountId.random();
        final var contributorId1 = ContributorId.random();
        final var contributorAccount1 = AccountId.random();
        final var contributorId2 = ContributorId.random();
        final var contributorAccount2 = AccountId.random();
        final var accountBook = AccountBookAggregate.fromEvents(
                new MintEvent(sponsorAccount, PositiveAmount.of(100L))
        );

        when(currencyStorage.get(currency.id())).thenReturn(Optional.of(currency));
        when(accountBookStorage.get(currency)).thenReturn(accountBook);
        when(sponsorAccountProvider.get(sponsorId, currency)).thenReturn(Optional.of(sponsorAccount));
        when(committeeAccountProvider.get(committeeId, currency)).thenReturn(Optional.of(committeeAccount));
        when(projectAccountProvider.get(projectId1, currency)).thenReturn(Optional.of(projectAccount1));
        when(projectAccountProvider.get(projectId2, currency)).thenReturn(Optional.of(projectAccount2));
        when(contributorAccountProvider.get(contributorId1, currency)).thenReturn(Optional.of(contributorAccount1));
        when(contributorAccountProvider.get(contributorId2, currency)).thenReturn(Optional.of(contributorAccount2));

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

        // Then
        assertThat(accountBook.state().balanceOf(sponsorAccount)).isEqualTo(PositiveAmount.of(15L));
        assertThat(accountBook.state().balanceOf(committeeAccount)).isEqualTo(PositiveAmount.of(0L));
        assertThat(accountBook.state().balanceOf(projectAccount1)).isEqualTo(PositiveAmount.of(0L));
        assertThat(accountBook.state().balanceOf(projectAccount2)).isEqualTo(PositiveAmount.of(30L));

        assertThat(accountBook.state().balanceOf(contributorAccount1)).isEqualTo(PositiveAmount.of(10L));
        assertThat(accountBook.state().transferredAmount(projectAccount1, contributorAccount1)).isEqualTo(PositiveAmount.of(10L));
        assertThat(accountBook.state().transferredAmount(sponsorAccount, contributorAccount1)).isEqualTo(PositiveAmount.of(10L));
        assertThat(accountBook.state().transferredAmount(committeeAccount, contributorAccount1)).isEqualTo(PositiveAmount.of(10L));

        assertThat(accountBook.state().balanceOf(contributorAccount2)).isEqualTo(PositiveAmount.of(45L));
        assertThat(accountBook.state().transferredAmount(projectAccount1, contributorAccount2)).isEqualTo(PositiveAmount.of(20L));
        assertThat(accountBook.state().transferredAmount(projectAccount2, contributorAccount2)).isEqualTo(PositiveAmount.of(25L));
        assertThat(accountBook.state().transferredAmount(sponsorAccount, contributorAccount2)).isEqualTo(PositiveAmount.of(45L));
        assertThat(accountBook.state().transferredAmount(committeeAccount, contributorAccount2)).isEqualTo(PositiveAmount.of(40L));

        verify(accountBookStorage, times(9)).save(accountBook);
    }
}
