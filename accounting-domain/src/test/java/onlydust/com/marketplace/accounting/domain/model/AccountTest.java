package onlydust.com.marketplace.accounting.domain.model;

import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


class AccountTest {

    @Test
    void registerTransfer() {
        final var account = new Account(Currency.Usd);
        account.registerExternalTransfer(Amount.of(500L, Currency.Usd));
        assertThat(account.balance()).isEqualTo(Amount.of(500L, Currency.Usd));

        account.registerExternalTransfer(Amount.of(-500L, Currency.Usd));
        assertThat(account.balance()).isEqualTo(Amount.of(0L, Currency.Usd));

        assertThatThrownBy(() -> account.registerExternalTransfer(Amount.of(-500L, Currency.Usd)))
                .isInstanceOf(OnlyDustException.class)
                .hasMessage("Insufficient funds");
    }

    @Test
    void registerTransfer_with_other_currency() {
        final var account = new Account(Currency.Usd);
        assertThatThrownBy(() -> account.registerExternalTransfer(Amount.of(500L, Currency.Eth)))
                .isInstanceOf(OnlyDustException.class)
                .hasMessage("Cannot sum different currencies");
    }

    @Test
    void transferTo() {
        final var account1 = new Account(PositiveAmount.of(1000L, Currency.Usd));
        final var account2 = new Account(Currency.Usd);

        account1.sendAmountTo(account2, PositiveAmount.of(200L, Currency.Usd));

        assertThat(account1.balance()).isEqualTo(Amount.of(800L, Currency.Usd));
        assertThat(account2.balance()).isEqualTo(Amount.of(200L, Currency.Usd));
    }

    @Test
    void transferTo_account_with_other_currency() {
        final var account1 = new Account(PositiveAmount.of(1000L, Currency.Usd));
        final var account2 = new Account(Currency.Eth);

        assertThatThrownBy(() -> account1.sendAmountTo(account2, PositiveAmount.of(500L, Currency.Usd)))
                .isInstanceOf(OnlyDustException.class)
                .hasMessage("Cannot sum different currencies");

        assertThatThrownBy(() -> account1.sendAmountTo(account2, PositiveAmount.of(500L, Currency.Eth)))
                .isInstanceOf(OnlyDustException.class)
                .hasMessage("Cannot sum different currencies");
    }
}