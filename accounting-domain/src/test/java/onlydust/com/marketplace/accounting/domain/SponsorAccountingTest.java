package onlydust.com.marketplace.accounting.domain;

import onlydust.com.marketplace.accounting.domain.model.Account;
import onlydust.com.marketplace.accounting.domain.model.Amount;
import onlydust.com.marketplace.accounting.domain.model.Currency;
import onlydust.com.marketplace.accounting.domain.model.SponsorId;
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
    SponsorId sponsorId;

    private static final Amount ONE_USD = Amount.of(BigDecimal.ONE, Currency.Usd);
    private static final Amount ONE_ETH = Amount.of(BigDecimal.ONE, Currency.Eth);

    @BeforeEach
    void setUp() {
        sponsorAccountProvider = mock(SponsorAccountProvider.class);
        sponsorAccounting = new SponsorAccounting(sponsorAccountProvider);
        sponsorId = SponsorId.random();
    }

    @Test
    public void should_register_transfer_from_sponsor() {
        when(sponsorAccountProvider.sponsorAccount(sponsorId, Currency.Usd)).thenReturn(Optional.of(new Account(Currency.Usd)));
        sponsorAccounting.registerTransfer(sponsorId, ONE_USD);
    }

    @Test
    public void should_fail_to_register_transfer_from_unknown_sponsor() {
        final var otherSponsorId = SponsorId.random();
        when(sponsorAccountProvider.sponsorAccount(otherSponsorId, Currency.Usd)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> sponsorAccounting.registerTransfer(otherSponsorId, ONE_USD))
                .isInstanceOf(OnlyDustException.class).hasMessage("Sponsor %s USD account not found".formatted(otherSponsorId));
    }

    @Test
    public void should_fail_to_register_transfer_from_sponsor_for_unregistered_currency() {
        when(sponsorAccountProvider.sponsorAccount(sponsorId, Currency.Usd)).thenReturn(Optional.of(new Account(Currency.Usd)));
        when(sponsorAccountProvider.sponsorAccount(sponsorId, Currency.Eth)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> sponsorAccounting.registerTransfer(sponsorId, ONE_ETH))
                .isInstanceOf(OnlyDustException.class).hasMessage("Sponsor %s ETH account not found".formatted(sponsorId));
    }

    @Test
    public void should_register_transfer_to_sponsor() {
        when(sponsorAccountProvider.sponsorAccount(sponsorId, Currency.Usd)).thenReturn(Optional.of(new Account(Amount.of(BigDecimal.ONE, Currency.Usd))));
        sponsorAccounting.registerTransfer(sponsorId, ONE_USD);
    }

    @Test
    public void should_fail_to_register_transfer_to_sponsor_higher_than_what_it_sent() {
        when(sponsorAccountProvider.sponsorAccount(sponsorId, Currency.Usd)).thenReturn(Optional.of(new Account(Currency.Usd)));
        assertThatThrownBy(() -> sponsorAccounting.registerTransfer(sponsorId, Amount.of(-1L,
                Currency.Usd)))
                .isInstanceOf(OnlyDustException.class)
                .hasMessage("Insufficient funds");
    }

    @Test
    public void should_add_up_transfers() {
        when(sponsorAccountProvider.sponsorAccount(sponsorId, Currency.Usd)).thenReturn(Optional.of(new Account(Currency.Usd)));
        sponsorAccounting.registerTransfer(sponsorId, Amount.of(1L, Currency.Usd));
        sponsorAccounting.registerTransfer(sponsorId, Amount.of(3L, Currency.Usd));
        sponsorAccounting.registerTransfer(sponsorId, Amount.of(-2L, Currency.Usd));
        sponsorAccounting.registerTransfer(sponsorId, Amount.of(-2L, Currency.Usd));
        assertThatThrownBy(() -> sponsorAccounting.registerTransfer(sponsorId, Amount.of(-1L,
                Currency.Usd)))
                .isInstanceOf(OnlyDustException.class)
                .hasMessage("Insufficient funds");
    }

    @Test
    public void should_add_up_transfers_of_different_currencies_separately() {
        when(sponsorAccountProvider.sponsorAccount(sponsorId, Currency.Usd)).thenReturn(Optional.of(new Account(Currency.Usd)));
        when(sponsorAccountProvider.sponsorAccount(sponsorId, Currency.Eth)).thenReturn(Optional.of(new Account(Currency.Eth)));
        sponsorAccounting.registerTransfer(sponsorId, Amount.of(1L, Currency.Eth));
        sponsorAccounting.registerTransfer(sponsorId, Amount.of(3L, Currency.Usd));
        assertThatThrownBy(() -> sponsorAccounting.registerTransfer(sponsorId, Amount.of(-2L, Currency.Eth)))
                .isInstanceOf(OnlyDustException.class)
                .hasMessage("Insufficient funds");
    }
}
