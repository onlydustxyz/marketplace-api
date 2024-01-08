package onlydust.com.marketplace.accounting.domain.model;

import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AmountTest {

    @Test
    void plus() {
        final var amount1 = Amount.of(3L, Currency.Usd);
        final var amount2 = Amount.of(2L, Currency.Usd);

        final var result = amount1.plus(amount2);
        assertThat(result).isEqualTo(Amount.of(5L, Currency.Usd));
    }

    @Test
    void should_not_add_different_currencies() {
        final var amount1 = Amount.of(3L, Currency.Usd);
        final var amount2 = Amount.of(2L, Currency.Eth);

        assertThatThrownBy(() -> amount1.plus(amount2))
                .isInstanceOf(OnlyDustException.class)
                .hasMessage("Cannot sum different currencies");
    }

    @Test
    void isPositive() {
        assertThat(Amount.of(3L, Currency.Usd).isPositive()).isTrue();
        assertThat(Amount.of(-3L, Currency.Usd).isPositive()).isFalse();
    }

    @Test
    void isNegative() {
        assertThat(Amount.of(3L, Currency.Usd).isNegative()).isFalse();
        assertThat(Amount.of(-3L, Currency.Usd).isNegative()).isTrue();
    }

    @Test
    void testEquals() {
        final var amount1 = Amount.of(3L, Currency.Usd);
        final var amount2 = Amount.of(3L, Currency.Usd);
        final var amount3 = Amount.of(2L, Currency.Usd);

        assertThat(amount1).isEqualTo(amount2);
        assertThat(amount1).isNotEqualTo(amount3);
    }

    @Test
    void testEquals_with_different_currencies() {
        final var amount1 = Amount.of(3L, Currency.Usd);
        final var amount2 = Amount.of(3L, Currency.Eth);

        assertThat(amount1).isNotEqualTo(amount2);
    }
}