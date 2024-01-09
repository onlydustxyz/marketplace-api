package onlydust.com.marketplace.accounting.domain;

import onlydust.com.marketplace.accounting.domain.model.*;
import onlydust.com.marketplace.accounting.domain.port.in.SponsorAccountingFacadePort;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SponsorAccountingTest {
    SponsorAccountingFacadePort sponsorAccounting;
    SponsorAccountProvider sponsorAccountProvider;
    CommitteeAccountProvider committeeAccountProvider;
    SponsorId sponsorId;
    CommitteeId committeeId;

    private static final Amount ONE_USD = Amount.of(BigDecimal.ONE, Currency.Usd);
    private static final Amount ONE_ETH = Amount.of(BigDecimal.ONE, Currency.Eth);

    @BeforeEach
    void setUp() {
        sponsorAccountProvider = mock(SponsorAccountProvider.class);
        committeeAccountProvider = mock(CommitteeAccountProvider.class);
        sponsorAccounting = new SponsorAccounting(sponsorAccountProvider, committeeAccountProvider);
        sponsorId = SponsorId.random();
        committeeId = CommitteeId.random();
    }

    @Test
    public void should_register_transfer_from_sponsor() {
        when(sponsorAccountProvider.get(sponsorId, Currency.Usd)).thenReturn(Optional.of(new Account(Currency.Usd)));
        sponsorAccounting.registerTransfer(sponsorId, ONE_USD);
    }

    @Test
    public void should_fail_to_register_transfer_from_unknown_sponsor() {
        final var otherSponsorId = SponsorId.random();
        when(sponsorAccountProvider.get(otherSponsorId, Currency.Usd)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> sponsorAccounting.registerTransfer(otherSponsorId, ONE_USD))
                .isInstanceOf(OnlyDustException.class).hasMessage("Sponsor %s USD account not found".formatted(otherSponsorId));
    }

    @Test
    public void should_fail_to_register_transfer_from_sponsor_for_unregistered_currency() {
        when(sponsorAccountProvider.get(sponsorId, Currency.Usd)).thenReturn(Optional.of(new Account(Currency.Usd)));
        when(sponsorAccountProvider.get(sponsorId, Currency.Eth)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> sponsorAccounting.registerTransfer(sponsorId, ONE_ETH))
                .isInstanceOf(OnlyDustException.class).hasMessage("Sponsor %s ETH account not found".formatted(sponsorId));
    }

    @Test
    public void should_register_transfer_to_sponsor() {
        when(sponsorAccountProvider.get(sponsorId, Currency.Usd)).thenReturn(Optional.of(new Account(PositiveAmount.of(BigDecimal.ONE, Currency.Usd))));
        sponsorAccounting.registerTransfer(sponsorId, ONE_USD);
    }

    @Test
    public void should_fail_to_register_transfer_to_unknown_sponsor() {
        final var otherSponsorId = SponsorId.random();
        when(sponsorAccountProvider.get(otherSponsorId, Currency.Usd)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> sponsorAccounting.registerTransfer(otherSponsorId, ONE_USD))
                .isInstanceOf(OnlyDustException.class).hasMessage("Sponsor %s USD account not found".formatted(otherSponsorId));
    }

    @Test
    public void should_fail_to_register_transfer_to_sponsor_higher_than_what_it_sent() {
        when(sponsorAccountProvider.get(sponsorId, Currency.Usd)).thenReturn(Optional.of(new Account(Currency.Usd)));
        assertThatThrownBy(() -> sponsorAccounting.registerTransfer(sponsorId, Amount.of(-1L,
                Currency.Usd)))
                .isInstanceOf(OnlyDustException.class)
                .hasMessage("Cannot register transfer of -1USD for sponsor %s: Insufficient funds".formatted(sponsorId));
    }

    @Test
    public void should_add_up_transfers() {
        when(sponsorAccountProvider.get(sponsorId, Currency.Usd)).thenReturn(Optional.of(new Account(Currency.Usd)));
        sponsorAccounting.registerTransfer(sponsorId, Amount.of(1L, Currency.Usd));
        sponsorAccounting.registerTransfer(sponsorId, Amount.of(3L, Currency.Usd));
        sponsorAccounting.registerTransfer(sponsorId, Amount.of(-2L, Currency.Usd));
        sponsorAccounting.registerTransfer(sponsorId, Amount.of(-2L, Currency.Usd));
        assertThatThrownBy(() -> sponsorAccounting.registerTransfer(sponsorId, Amount.of(-1L,
                Currency.Usd)))
                .isInstanceOf(OnlyDustException.class)
                .hasMessage("Cannot register transfer of -1USD for sponsor %s: Insufficient funds".formatted(sponsorId));
    }

    @Test
    public void should_add_up_transfers_of_different_currencies_separately() {
        when(sponsorAccountProvider.get(sponsorId, Currency.Usd)).thenReturn(Optional.of(new Account(Currency.Usd)));
        when(sponsorAccountProvider.get(sponsorId, Currency.Eth)).thenReturn(Optional.of(new Account(Currency.Eth)));
        sponsorAccounting.registerTransfer(sponsorId, Amount.of(1L, Currency.Eth));
        sponsorAccounting.registerTransfer(sponsorId, Amount.of(3L, Currency.Usd));
        assertThatThrownBy(() -> sponsorAccounting.registerTransfer(sponsorId, Amount.of(-2L, Currency.Eth)))
                .isInstanceOf(OnlyDustException.class)
                .hasMessage("Cannot register transfer of -2ETH for sponsor %s: Insufficient funds".formatted(sponsorId));
    }

    @Test
    public void should_fund_a_committee() {
        when(sponsorAccountProvider.get(sponsorId, Currency.Usd)).thenReturn(Optional.of(new Account(PositiveAmount.of(7L,
                Currency.Usd))));
        when(committeeAccountProvider.get(committeeId, Currency.Usd)).thenReturn(Optional.of(new Account(Currency.Usd)));
        sponsorAccounting.fundCommittee(sponsorId, committeeId, PositiveAmount.of(1L, Currency.Usd));
    }

    @Test
    public void should_fail_to_fund_an_unknown_committee() {
        when(sponsorAccountProvider.get(sponsorId, Currency.Usd)).thenReturn(Optional.of(new Account(PositiveAmount.of(1L,
                Currency.Usd))));
        when(committeeAccountProvider.get(committeeId, Currency.Usd)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> sponsorAccounting.fundCommittee(sponsorId, committeeId, PositiveAmount.of(2L,
                Currency.Usd)))
                .isInstanceOf(OnlyDustException.class)
                .hasMessage("Committee %s USD account not found".formatted(committeeId));
    }

    @Test
    public void should_fail_to_fund_a_committee_if_balance_is_insufficient() {
        when(sponsorAccountProvider.get(sponsorId, Currency.Usd)).thenReturn(Optional.of(new Account(PositiveAmount.of(1L,
                Currency.Usd))));
        when(committeeAccountProvider.get(committeeId, Currency.Usd)).thenReturn(Optional.of(new Account(Currency.Usd)));
        assertThatThrownBy(() -> sponsorAccounting.fundCommittee(sponsorId, committeeId, PositiveAmount.of(2L,
                Currency.Usd)))
                .isInstanceOf(OnlyDustException.class)
                .hasMessage("Cannot transfer 2USD from sponsor %s to committee %s: Insufficient funds".formatted(sponsorId, committeeId));
    }

    @Test
    public void should_refund_sponsor_from_a_committee() {
        when(sponsorAccountProvider.get(sponsorId, Currency.Usd)).thenReturn(Optional.of(new Account(PositiveAmount.of(7L,
                Currency.Usd))));
        when(committeeAccountProvider.get(committeeId, Currency.Usd)).thenReturn(Optional.of(new Account(Currency.Usd)));
        sponsorAccounting.fundCommittee(sponsorId, committeeId, PositiveAmount.of(1L, Currency.Usd));
        sponsorAccounting.refundFromCommittee(committeeId, sponsorId, PositiveAmount.of(1L, Currency.Usd));
    }

    @Test
    public void should_fail_to_refund_sponsor_from_a_committee_if_balance_is_insufficient() {
        when(sponsorAccountProvider.get(sponsorId, Currency.Usd)).thenReturn(Optional.of(new Account(PositiveAmount.of(7L,
                Currency.Usd))));
        when(committeeAccountProvider.get(committeeId, Currency.Usd)).thenReturn(Optional.of(new Account(Currency.Usd)));
        sponsorAccounting.fundCommittee(sponsorId, committeeId, PositiveAmount.of(1L, Currency.Usd));
        assertThatThrownBy(() -> sponsorAccounting.refundFromCommittee(committeeId, sponsorId, PositiveAmount.of(2L,
                Currency.Usd)))
                .isInstanceOf(OnlyDustException.class)
                .hasMessage(("Cannot transfer 2USD from committee %s to sponsor %s: Cannot refund more than the amount" +
                             " received").formatted(committeeId, sponsorId));
    }

    @Test
    public void should_fail_to_refund_sponsor_from_a_committee_if_amount_was_given_by_another_sponsor() {
        when(sponsorAccountProvider.get(sponsorId, Currency.Usd)).thenReturn(Optional.of(new Account(PositiveAmount.of(7L,
                Currency.Usd))));
        when(committeeAccountProvider.get(committeeId, Currency.Usd)).thenReturn(Optional.of(new Account(Currency.Usd)));
        sponsorAccounting.fundCommittee(sponsorId, committeeId, PositiveAmount.of(5L, Currency.Usd));

        final var otherCommitteeId = CommitteeId.random();
        when(committeeAccountProvider.get(otherCommitteeId, Currency.Usd)).thenReturn(Optional.of(new Account(PositiveAmount.of(6L, Currency.Usd))));


        assertThatThrownBy(() -> sponsorAccounting.refundFromCommittee(otherCommitteeId, sponsorId,
                PositiveAmount.of(2L,
                        Currency.Usd)))
                .isInstanceOf(OnlyDustException.class)
                .hasMessage(("Cannot transfer 2USD from committee %s to sponsor %s: " +
                             "Cannot refund more than the amount received").formatted(otherCommitteeId, sponsorId));
    }


}
