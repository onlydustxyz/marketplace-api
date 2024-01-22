package onlydust.com.marketplace.accounting.domain;

import onlydust.com.marketplace.accounting.domain.model.*;
import onlydust.com.marketplace.accounting.domain.port.out.CurrencyStorage;
import onlydust.com.marketplace.accounting.domain.stubs.Currencies;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.assertj.core.api.Java6Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

public class SponsorAccountingServiceTest {
    final SponsorAccountProvider sponsorAccountProvider = mock(SponsorAccountProvider.class);
    final CommitteeAccountProvider committeeAccountProvider = mock(CommitteeAccountProvider.class);
    final CurrencyStorage currencyStorage = mock(CurrencyStorage.class);
    final SponsorAccountingService sponsorAccountingService = new SponsorAccountingService(sponsorAccountProvider, committeeAccountProvider, currencyStorage);

    @BeforeEach
    void setUp() {
        reset(sponsorAccountProvider, committeeAccountProvider, currencyStorage);
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
        final var account = new Account(currency);

        when(currencyStorage.get(currency.id())).thenReturn(Optional.of(currency));
        when(sponsorAccountProvider.get(sponsorId, currency)).thenReturn(Optional.empty());
        when(sponsorAccountProvider.create(sponsorId, currency)).thenReturn(account);

        // When
        sponsorAccountingService.receiveFrom(sponsorId, PositiveAmount.of(10L), currency.id());

        // Then
        assertThat(account.balance()).isEqualTo(Money.of(10L, currency));
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
        final var account = new Account(PositiveMoney.of(100L, currency));

        when(currencyStorage.get(currency.id())).thenReturn(Optional.of(currency));
        when(sponsorAccountProvider.get(sponsorId, currency)).thenReturn(Optional.of(account));

        // When
        sponsorAccountingService.receiveFrom(sponsorId, PositiveAmount.of(10L), currency.id());

        // Then
        assertThat(account.balance()).isEqualTo(Money.of(110L, currency));
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
        assertThatThrownBy(() -> sponsorAccountingService.receiveFrom(sponsorId, PositiveAmount.of(10L), currencyId))
                // Then
                .isInstanceOf(OnlyDustException.class)
                .hasMessage("Currency %s not found".formatted(currencyId));
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
        final var account = new Account(PositiveMoney.of(100L, currency));

        when(currencyStorage.get(currency.id())).thenReturn(Optional.of(currency));
        when(sponsorAccountProvider.get(sponsorId, currency)).thenReturn(Optional.of(account));

        // When
        sponsorAccountingService.refundTo(sponsorId, PositiveAmount.of(10L), currency.id());

        // Then
        assertThat(account.balance()).isEqualTo(Money.of(90L, currency));
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
        when(sponsorAccountProvider.get(sponsorId, currency)).thenReturn(Optional.empty());

        // When
        assertThatThrownBy(() -> sponsorAccountingService.refundTo(sponsorId, PositiveAmount.of(10L), currency.id()))
                // Then
                .isInstanceOf(OnlyDustException.class)
                .hasMessage("No account found for sponsor %s in currency %s".formatted(sponsorId, currency));
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
        assertThatThrownBy(() -> sponsorAccountingService.refundTo(sponsorId, PositiveAmount.of(10L), currencyId))
                // Then
                .isInstanceOf(OnlyDustException.class)
                .hasMessage("Currency %s not found".formatted(currencyId));
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
        final var sponsorAccount = new Account(PositiveMoney.of(100L, currency));
        final var committeeId = CommitteeId.random();
        final var committeeAccount = new Account(PositiveMoney.of(200L, currency));

        when(currencyStorage.get(currency.id())).thenReturn(Optional.of(currency));
        when(sponsorAccountProvider.get(sponsorId, currency)).thenReturn(Optional.of(sponsorAccount));
        when(committeeAccountProvider.get(committeeId, currency)).thenReturn(Optional.of(committeeAccount));

        // When
        sponsorAccountingService.allocate(sponsorId, committeeId, PositiveAmount.of(10L), currency.id());

        // Then
        assertThat(sponsorAccount.balance()).isEqualTo(Money.of(90L, currency));
        assertThat(committeeAccount.balance()).isEqualTo(Money.of(210L, currency));
        assertThat(committeeAccount.balanceFrom(sponsorAccount.getId())).isEqualTo(Money.of(10L, currency));
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
        final var sponsorAccount = new Account(PositiveMoney.of(100L, currency));
        final var committeeId = CommitteeId.random();
        final var committeeAccount = new Account(currency);

        when(currencyStorage.get(currency.id())).thenReturn(Optional.of(currency));
        when(sponsorAccountProvider.get(sponsorId, currency)).thenReturn(Optional.of(sponsorAccount));
        when(committeeAccountProvider.get(committeeId, currency)).thenReturn(Optional.empty());
        when(committeeAccountProvider.create(committeeId, currency)).thenReturn(committeeAccount);

        // When
        sponsorAccountingService.allocate(sponsorId, committeeId, PositiveAmount.of(10L), currency.id());

        // Then
        assertThat(sponsorAccount.balance()).isEqualTo(Money.of(90L, currency));
        assertThat(committeeAccount.balance()).isEqualTo(Money.of(10L, currency));
        assertThat(committeeAccount.balanceFrom(sponsorAccount.getId())).isEqualTo(Money.of(10L, currency));
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
        assertThatThrownBy(() -> sponsorAccountingService.allocate(sponsorId, committeeId, PositiveAmount.of(10L), currencyId))
                // Then
                .isInstanceOf(OnlyDustException.class)
                .hasMessage("Currency %s not found".formatted(currencyId));
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
        final var sponsorAccount = new Account(PositiveMoney.of(100L, currency));
        final var committeeId = CommitteeId.random();
        final var committeeAccount = new Account(PositiveMoney.of(200L, currency));

        when(currencyStorage.get(currency.id())).thenReturn(Optional.of(currency));
        when(sponsorAccountProvider.get(sponsorId, currency)).thenReturn(Optional.of(sponsorAccount));
        when(committeeAccountProvider.get(committeeId, currency)).thenReturn(Optional.of(committeeAccount));

        // When
        sponsorAccountingService.allocate(sponsorId, committeeId, PositiveAmount.of(10L), currency.id());
        sponsorAccountingService.unallocate(sponsorId, committeeId, PositiveAmount.of(5L), currency.id());

        // Then
        assertThat(sponsorAccount.balance()).isEqualTo(Money.of(95L, currency));
        assertThat(committeeAccount.balance()).isEqualTo(Money.of(205L, currency));
        assertThat(committeeAccount.balanceFrom(sponsorAccount.getId())).isEqualTo(Money.of(5L, currency));
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
        assertThatThrownBy(() -> sponsorAccountingService.unallocate(sponsorId, committeeId, PositiveAmount.of(10L), currencyId))
                // Then
                .isInstanceOf(OnlyDustException.class)
                .hasMessage("Currency %s not found".formatted(currencyId));
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
        when(sponsorAccountProvider.get(sponsorId, currency)).thenReturn(Optional.empty());

        // When
        assertThatThrownBy(() -> sponsorAccountingService.unallocate(sponsorId, committeeId, PositiveAmount.of(10L), currency.id()))
                // Then
                .isInstanceOf(OnlyDustException.class)
                .hasMessage("No account found for sponsor %s in currency %s".formatted(sponsorId, currency));
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
        final var sponsorAccount = new Account(PositiveMoney.of(100L, currency));

        when(currencyStorage.get(currency.id())).thenReturn(Optional.of(currency));
        when(sponsorAccountProvider.get(sponsorId, currency)).thenReturn(Optional.of(sponsorAccount));
        when(committeeAccountProvider.get(committeeId, currency)).thenReturn(Optional.empty());

        // When
        assertThatThrownBy(() -> sponsorAccountingService.unallocate(sponsorId, committeeId, PositiveAmount.of(10L), currency.id()))
                // Then
                .isInstanceOf(OnlyDustException.class)
                .hasMessage("No account found for committee %s in currency %s".formatted(committeeId, currency));
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
        final var sponsorAccount = new Account(PositiveMoney.of(100L, currency));
        final var committeeId = CommitteeId.random();
        final var committeeAccount = new Account(PositiveMoney.of(200L, currency));

        when(currencyStorage.get(currency.id())).thenReturn(Optional.of(currency));
        when(sponsorAccountProvider.get(sponsorId, currency)).thenReturn(Optional.of(sponsorAccount));
        when(committeeAccountProvider.get(committeeId, currency)).thenReturn(Optional.of(committeeAccount));

        // When
        assertThatThrownBy(() -> sponsorAccountingService.unallocate(sponsorId, committeeId, PositiveAmount.of(10L), currency.id()))
                // Then
                .isInstanceOf(OnlyDustException.class)
                .hasMessage("Cannot refund more than the amount received");
    }
}
