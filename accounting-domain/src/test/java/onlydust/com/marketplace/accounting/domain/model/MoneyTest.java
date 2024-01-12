package onlydust.com.marketplace.accounting.domain.model;

import onlydust.com.marketplace.accounting.domain.stubs.Currencies;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MoneyTest {

    @Test
    void add() {
        final var amount1 = Money.of(3L, Currencies.USD);
        final var amount2 = Money.of(2L, Currencies.USD);

        final var result = amount1.add(amount2);
        assertThat(result).isEqualTo(Money.of(5L, Currencies.USD));
    }

    @Test
    void should_not_add_different_currencies() {
        final var amount1 = Money.of(3L, Currencies.USD);
        final var amount2 = Money.of(2L, Currencies.ETH);

        assertThatThrownBy(() -> amount1.add(amount2))
                .isInstanceOf(OnlyDustException.class)
                .hasMessage("Cannot perform arithmetic operations with different currencies");
    }

    @Test
    void subtract() {
        final var amount1 = Money.of(3L, Currencies.USD);
        final var amount2 = Money.of(2L, Currencies.USD);

        final var result = amount1.subtract(amount2);
        assertThat(result).isEqualTo(Money.of(1L, Currencies.USD));
    }

    @Test
    void should_not_subtract_different_currencies() {
        final var amount1 = Money.of(3L, Currencies.USD);
        final var amount2 = Money.of(2L, Currencies.ETH);

        assertThatThrownBy(() -> amount1.subtract(amount2))
                .isInstanceOf(OnlyDustException.class)
                .hasMessage("Cannot perform arithmetic operations with different currencies");
    }

    @Test
    void negate() {
        final var amount = Money.of(3L, Currencies.USD);
        final var result = amount.negate();
        assertThat(result).isEqualTo(Money.of(-3L, Currencies.USD));
    }

    @Test
    void isPositive() {
        assertThat(Money.of(3L, Currencies.USD).isPositive()).isTrue();
        assertThat(Money.of(-3L, Currencies.USD).isPositive()).isFalse();
    }

    @Test
    void isNegative() {
        assertThat(Money.of(3L, Currencies.USD).isNegative()).isFalse();
        assertThat(Money.of(-3L, Currencies.USD).isNegative()).isTrue();
    }

    @Test
    void isLowerThan() {
        final var amount1 = Money.of(3L, Currencies.USD);
        final var amount2 = Money.of(2L, Currencies.USD);

        assertThat(amount1.isStrictlyLowerThan(amount2)).isFalse();
        assertThat(amount2.isStrictlyLowerThan(amount1)).isTrue();
        assertThat(amount1.isStrictlyLowerThan(amount1)).isFalse();
    }

    @Test
    void testEquals() {
        final var amount1 = Money.of(3L, Currencies.USD);
        final var amount2 = Money.of(3L, Currencies.USD);
        final var amount3 = Money.of(2L, Currencies.USD);

        assertThat(amount1).isEqualTo(amount2);
        assertThat(amount1).isNotEqualTo(amount3);
    }

    @Test
    void testEquals_with_different_currencies() {
        final var amount1 = Money.of(3L, Currencies.USD);
        final var amount2 = Money.of(3L, Currencies.ETH);

        assertThat(amount1).isNotEqualTo(amount2);
    }
}