package onlydust.com.marketplace.accounting.domain;

import onlydust.com.marketplace.accounting.domain.model.*;
import onlydust.com.marketplace.accounting.domain.port.out.CurrencyStorage;
import onlydust.com.marketplace.accounting.domain.stubs.Currencies;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.Mockito.*;

public class SponsorAccountingServiceTest {
    final SponsorAccountProvider sponsorAccountProvider = mock(SponsorAccountProvider.class);
    final CurrencyStorage currencyStorage = mock(CurrencyStorage.class);
    final SponsorAccountingService sponsorAccountingService = new SponsorAccountingService(sponsorAccountProvider, currencyStorage);

    @BeforeEach
    void setUp() {
        reset(sponsorAccountProvider, currencyStorage);
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
}
