package onlydust.com.marketplace.accounting.domain;

import onlydust.com.marketplace.accounting.domain.model.Account;
import onlydust.com.marketplace.accounting.domain.model.Amount;
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

    @BeforeEach
    void setUp() {
        sponsorAccountProvider = mock(SponsorAccountProvider.class);
        sponsorAccounting = new SponsorAccounting(sponsorAccountProvider);
        sponsorId = SponsorId.random();
    }

    @Test
    public void should_register_transfer_from_sponsor() {
        when(sponsorAccountProvider.sponsorAccount(sponsorId)).thenReturn(Optional.of(new Account()));
        sponsorAccounting.registerTransfer(sponsorId, Amount.of(BigDecimal.ONE));
    }

    @Test
    public void should_fail_to_register_transfer_from_unknown_sponsor() {
        final var otherSponsorId = SponsorId.random();
        when(sponsorAccountProvider.sponsorAccount(otherSponsorId)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> sponsorAccounting.registerTransfer(otherSponsorId,
                Amount.of(BigDecimal.ONE)))
                .isInstanceOf(OnlyDustException.class).hasMessage("Sponsor account not found");
    }

    @Test
    public void should_register_transfer_to_sponsor() {
        when(sponsorAccountProvider.sponsorAccount(sponsorId)).thenReturn(Optional.of(new Account(Amount.of(BigDecimal.ONE))));
        sponsorAccounting.registerTransfer(sponsorId, Amount.of(BigDecimal.ONE));
    }

    @Test
    public void should_fail_to_register_transfer_to_sponsor_bigger_than_its_balance() {
        when(sponsorAccountProvider.sponsorAccount(sponsorId)).thenReturn(Optional.of(new Account()));
        assertThatThrownBy(() -> sponsorAccounting.registerTransfer(sponsorId, Amount.of(BigDecimal.valueOf(-1L))))
                .isInstanceOf(OnlyDustException.class)
                .hasMessage("Insufficient funds");
    }

    @Test
    public void should_add_up_transfers() {
        when(sponsorAccountProvider.sponsorAccount(sponsorId)).thenReturn(Optional.of(new Account()));
        sponsorAccounting.registerTransfer(sponsorId, Amount.of(BigDecimal.valueOf(1L)));
        sponsorAccounting.registerTransfer(sponsorId, Amount.of(BigDecimal.valueOf(3L)));
        sponsorAccounting.registerTransfer(sponsorId, Amount.of(BigDecimal.valueOf(-2L)));
        sponsorAccounting.registerTransfer(sponsorId, Amount.of(BigDecimal.valueOf(-2L)));
        assertThatThrownBy(() -> sponsorAccounting.registerTransfer(sponsorId, Amount.of(BigDecimal.valueOf(-1L))))
                .isInstanceOf(OnlyDustException.class)
                .hasMessage("Insufficient funds");
    }

    @Test
    public void should_fail_to_register_transfer_to_sponsor_bigger_than_its_balance_for_a_specific_currency() {
        when(sponsorAccountProvider.sponsorAccount(sponsorId)).thenReturn(Optional.of(new Account(BigDecimal.ONE)));
        assertThatThrownBy(() -> sponsorAccounting.registerTransfer(sponsorId, Amount.of(BigDecimal.valueOf(-1L))))
                .isInstanceOf(OnlyDustException.class)
                .hasMessage("Insufficient funds");
    }
}
