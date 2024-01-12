package onlydust.com.marketplace.accounting.domain.model;

import onlydust.com.marketplace.accounting.domain.stubs.Currencies;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


class AccountTest {

    @Test
    void mint_and_burn() {
        final var account = new Account(Currencies.USD);
        account.mint(PositiveMoney.of(500L, Currencies.USD));
        assertThat(account.balance()).isEqualTo(Money.of(500L, Currencies.USD));

        account.burn(PositiveMoney.of(500L, Currencies.USD));
        assertThat(account.balance()).isEqualTo(Money.of(0L, Currencies.USD));

        assertThatThrownBy(() -> account.burn(PositiveMoney.of(500L, Currencies.USD)))
                .isInstanceOf(OnlyDustException.class)
                .hasMessage("Insufficient funds");
    }

    @Test
    void mint_and_burn_with_other_currency() {
        final var account = new Account(Currencies.USD);
        assertThatThrownBy(() -> account.burn(PositiveMoney.of(500L, Currencies.ETH)))
                .isInstanceOf(OnlyDustException.class)
                .hasMessage("Cannot compare different currencies");
    }

    @Test
    void transferTo() {
        final var account1 = new Account(PositiveMoney.of(1000L, Currencies.USD));
        final var account2 = new Account(Currencies.USD);

        account1.send(account2, PositiveMoney.of(200L, Currencies.USD));

        assertThat(account1.balance()).isEqualTo(Money.of(800L, Currencies.USD));
        assertThat(account2.balance()).isEqualTo(Money.of(200L, Currencies.USD));
    }

    @Test
    void transferTo_account_with_other_currency() {
        final var account1 = new Account(PositiveMoney.of(1000L, Currencies.USD));
        final var account2 = new Account(Currencies.ETH);

        assertThatThrownBy(() -> account1.send(account2, PositiveMoney.of(500L, Currencies.USD)))
                .isInstanceOf(OnlyDustException.class)
                .hasMessage("ETH account cannot receive transactions in USD");

        assertThatThrownBy(() -> account1.send(account2, PositiveMoney.of(500L, Currencies.ETH)))
                .isInstanceOf(OnlyDustException.class)
                .hasMessage("Cannot compare different currencies");
    }
}