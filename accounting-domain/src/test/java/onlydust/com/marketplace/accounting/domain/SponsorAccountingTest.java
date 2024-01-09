package onlydust.com.marketplace.accounting.domain;

import onlydust.com.marketplace.accounting.domain.model.*;
import onlydust.com.marketplace.accounting.domain.port.in.SponsorAccountingFacadePort;
import onlydust.com.marketplace.accounting.domain.stubs.Currencies;
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

    private static final Amount ONE_USD = Amount.of(BigDecimal.ONE, Currencies.USD);
    private static final Amount ONE_ETH = Amount.of(BigDecimal.ONE, Currencies.ETH);

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
        when(sponsorAccountProvider.get(sponsorId, Currencies.USD)).thenReturn(Optional.of(new Account(Currencies.USD)));
        sponsorAccounting.registerTransfer(sponsorId, ONE_USD);
    }

    @Test
    public void should_fail_to_register_transfer_from_unknown_sponsor() {
        final var otherSponsorId = SponsorId.random();
        when(sponsorAccountProvider.get(otherSponsorId, Currencies.USD)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> sponsorAccounting.registerTransfer(otherSponsorId, ONE_USD))
                .isInstanceOf(OnlyDustException.class).hasMessage("Sponsor %s USD account not found".formatted(otherSponsorId));
    }

    @Test
    public void should_fail_to_register_transfer_from_sponsor_for_unregistered_currency() {
        when(sponsorAccountProvider.get(sponsorId, Currencies.USD)).thenReturn(Optional.of(new Account(Currencies.USD)));
        when(sponsorAccountProvider.get(sponsorId, Currencies.ETH)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> sponsorAccounting.registerTransfer(sponsorId, ONE_ETH))
                .isInstanceOf(OnlyDustException.class).hasMessage("Sponsor %s ETH account not found".formatted(sponsorId));
    }

    @Test
    public void should_register_transfer_to_sponsor() {
        when(sponsorAccountProvider.get(sponsorId, Currencies.USD)).thenReturn(Optional.of(new Account(PositiveAmount.of(BigDecimal.ONE, Currencies.USD))));
        sponsorAccounting.registerTransfer(sponsorId, ONE_USD);
    }

    @Test
    public void should_fail_to_register_transfer_to_unknown_sponsor() {
        final var otherSponsorId = SponsorId.random();
        when(sponsorAccountProvider.get(otherSponsorId, Currencies.USD)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> sponsorAccounting.registerTransfer(otherSponsorId, ONE_USD))
                .isInstanceOf(OnlyDustException.class).hasMessage("Sponsor %s USD account not found".formatted(otherSponsorId));
    }

    @Test
    public void should_fail_to_register_transfer_to_sponsor_higher_than_what_it_sent() {
        when(sponsorAccountProvider.get(sponsorId, Currencies.USD)).thenReturn(Optional.of(new Account(Currencies.USD)));
        assertThatThrownBy(() -> sponsorAccounting.registerTransfer(sponsorId, Amount.of(-1L,
                Currencies.USD)))
                .isInstanceOf(OnlyDustException.class)
                .hasMessage("Cannot register transfer of -1 USD for sponsor %s: Insufficient funds".formatted(sponsorId));
    }

    @Test
    public void should_add_up_transfers() {
        when(sponsorAccountProvider.get(sponsorId, Currencies.USD)).thenReturn(Optional.of(new Account(Currencies.USD)));
        sponsorAccounting.registerTransfer(sponsorId, Amount.of(1L, Currencies.USD));
        sponsorAccounting.registerTransfer(sponsorId, Amount.of(3L, Currencies.USD));
        sponsorAccounting.registerTransfer(sponsorId, Amount.of(-2L, Currencies.USD));
        sponsorAccounting.registerTransfer(sponsorId, Amount.of(-2L, Currencies.USD));
        assertThatThrownBy(() -> sponsorAccounting.registerTransfer(sponsorId, Amount.of(-1L,
                Currencies.USD)))
                .isInstanceOf(OnlyDustException.class)
                .hasMessage("Cannot register transfer of -1 USD for sponsor %s: Insufficient funds".formatted(sponsorId));
    }

    @Test
    public void should_add_up_transfers_of_different_currencies_separately() {
        when(sponsorAccountProvider.get(sponsorId, Currencies.USD)).thenReturn(Optional.of(new Account(Currencies.USD)));
        when(sponsorAccountProvider.get(sponsorId, Currencies.ETH)).thenReturn(Optional.of(new Account(Currencies.ETH)));
        sponsorAccounting.registerTransfer(sponsorId, Amount.of(1L, Currencies.ETH));
        sponsorAccounting.registerTransfer(sponsorId, Amount.of(3L, Currencies.USD));
        assertThatThrownBy(() -> sponsorAccounting.registerTransfer(sponsorId, Amount.of(-2L, Currencies.ETH)))
                .isInstanceOf(OnlyDustException.class)
                .hasMessage("Cannot register transfer of -2 ETH for sponsor %s: Insufficient funds".formatted(sponsorId));
    }

    @Test
    public void should_fund_a_committee() {
        when(sponsorAccountProvider.get(sponsorId, Currencies.USD)).thenReturn(Optional.of(new Account(PositiveAmount.of(7L,
                Currencies.USD))));
        when(committeeAccountProvider.get(committeeId, Currencies.USD)).thenReturn(Optional.of(new Account(Currencies.USD)));
        sponsorAccounting.fundCommittee(sponsorId, committeeId, PositiveAmount.of(1L, Currencies.USD));
    }

    @Test
    public void should_fail_to_fund_an_unknown_committee() {
        when(sponsorAccountProvider.get(sponsorId, Currencies.USD)).thenReturn(Optional.of(new Account(PositiveAmount.of(1L,
                Currencies.USD))));
        when(committeeAccountProvider.get(committeeId, Currencies.USD)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> sponsorAccounting.fundCommittee(sponsorId, committeeId, PositiveAmount.of(2L,
                Currencies.USD)))
                .isInstanceOf(OnlyDustException.class)
                .hasMessage("Committee %s USD account not found".formatted(committeeId));
    }

    @Test
    public void should_fail_to_fund_a_committee_if_balance_is_insufficient() {
        when(sponsorAccountProvider.get(sponsorId, Currencies.USD)).thenReturn(Optional.of(new Account(PositiveAmount.of(1L,
                Currencies.USD))));
        when(committeeAccountProvider.get(committeeId, Currencies.USD)).thenReturn(Optional.of(new Account(Currencies.USD)));
        assertThatThrownBy(() -> sponsorAccounting.fundCommittee(sponsorId, committeeId, PositiveAmount.of(2L,
                Currencies.USD)))
                .isInstanceOf(OnlyDustException.class)
                .hasMessage("Cannot transfer 2 USD from sponsor %s to committee %s: Insufficient funds".formatted(sponsorId, committeeId));
    }

    @Test
    public void should_refund_sponsor_from_a_committee() {
        when(sponsorAccountProvider.get(sponsorId, Currencies.USD)).thenReturn(Optional.of(new Account(PositiveAmount.of(7L,
                Currencies.USD))));
        when(committeeAccountProvider.get(committeeId, Currencies.USD)).thenReturn(Optional.of(new Account(Currencies.USD)));
        sponsorAccounting.fundCommittee(sponsorId, committeeId, PositiveAmount.of(1L, Currencies.USD));
        sponsorAccounting.refundFromCommittee(committeeId, sponsorId, PositiveAmount.of(1L, Currencies.USD));
    }

    @Test
    public void should_fail_to_refund_sponsor_from_a_committee_if_balance_is_insufficient() {
        when(sponsorAccountProvider.get(sponsorId, Currencies.USD)).thenReturn(Optional.of(new Account(PositiveAmount.of(7L,
                Currencies.USD))));

        final var committeeAccount = new Account(Currencies.USD);
        when(committeeAccountProvider.get(committeeId, Currencies.USD)).thenReturn(Optional.of(committeeAccount));
        sponsorAccounting.fundCommittee(sponsorId, committeeId, PositiveAmount.of(7L, Currencies.USD));

        final var anotherAccount = new Account(Currencies.USD);
        committeeAccount.sendAmountTo(anotherAccount, PositiveAmount.of(6L, Currencies.USD));

        assertThatThrownBy(() -> sponsorAccounting.refundFromCommittee(committeeId, sponsorId, PositiveAmount.of(2L,
                Currencies.USD)))
                .isInstanceOf(OnlyDustException.class)
                .hasMessage(("Cannot transfer 2 USD from committee %s to sponsor %s: Insufficient funds").formatted(committeeId, sponsorId));
    }

    @Test
    public void should_fail_to_refund_sponsor_from_a_committee_if_amount_given_by_the_sponsor_is_insufficient() {
        when(sponsorAccountProvider.get(sponsorId, Currencies.USD)).thenReturn(Optional.of(new Account(PositiveAmount.of(7L,
                Currencies.USD))));
        when(committeeAccountProvider.get(committeeId, Currencies.USD)).thenReturn(Optional.of(new Account(Currencies.USD)));
        sponsorAccounting.fundCommittee(sponsorId, committeeId, PositiveAmount.of(1L, Currencies.USD));
        assertThatThrownBy(() -> sponsorAccounting.refundFromCommittee(committeeId, sponsorId, PositiveAmount.of(2L,
                Currencies.USD)))
                .isInstanceOf(OnlyDustException.class)
                .hasMessage(("Cannot transfer 2 USD from committee %s to sponsor %s: Cannot refund more than the amount" +
                             " received").formatted(committeeId, sponsorId));
    }

    @Test
    public void should_fail_to_refund_sponsor_from_a_committee_if_amount_was_given_by_another_sponsor() {
        when(sponsorAccountProvider.get(sponsorId, Currencies.USD)).thenReturn(Optional.of(new Account(PositiveAmount.of(7L,
                Currencies.USD))));
        when(committeeAccountProvider.get(committeeId, Currencies.USD)).thenReturn(Optional.of(new Account(Currencies.USD)));
        sponsorAccounting.fundCommittee(sponsorId, committeeId, PositiveAmount.of(5L, Currencies.USD));

        final var otherCommitteeId = CommitteeId.random();
        when(committeeAccountProvider.get(otherCommitteeId, Currencies.USD)).thenReturn(Optional.of(new Account(PositiveAmount.of(6L, Currencies.USD))));


        assertThatThrownBy(() -> sponsorAccounting.refundFromCommittee(otherCommitteeId, sponsorId,
                PositiveAmount.of(2L,
                        Currencies.USD)))
                .isInstanceOf(OnlyDustException.class)
                .hasMessage(("Cannot transfer 2 USD from committee %s to sponsor %s: " +
                             "Cannot refund more than the amount received").formatted(otherCommitteeId, sponsorId));
    }


}
