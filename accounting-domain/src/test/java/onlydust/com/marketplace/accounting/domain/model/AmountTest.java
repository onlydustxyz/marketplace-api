package onlydust.com.marketplace.accounting.domain.model;

import onlydust.com.marketplace.accounting.domain.stubs.Currencies;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AmountTest {

    @Test
    void plus() {
        final var amount1 = Amount.of(3L, Currencies.USD);
        final var amount2 = Amount.of(2L, Currencies.USD);

        final var result = amount1.plus(amount2);
        assertThat(result).isEqualTo(Amount.of(5L, Currencies.USD));
    }

    @Test
    void should_not_add_different_currencies() {
        final var amount1 = Amount.of(3L, Currencies.USD);
        final var amount2 = Amount.of(2L, Currencies.ETH);

        assertThatThrownBy(() -> amount1.plus(amount2))
                .isInstanceOf(OnlyDustException.class)
                .hasMessage("Cannot sum different currencies");
    }

    @Test
    void negate() {
        final var amount = Amount.of(3L, Currencies.USD);
        final var result = amount.negate();
        assertThat(result).isEqualTo(Amount.of(-3L, Currencies.USD));
    }

    @Test
    void isPositive() {
        assertThat(Amount.of(3L, Currencies.USD).isPositive()).isTrue();
        assertThat(Amount.of(-3L, Currencies.USD).isPositive()).isFalse();
    }

    @Test
    void isNegative() {
        assertThat(Amount.of(3L, Currencies.USD).isNegative()).isFalse();
        assertThat(Amount.of(-3L, Currencies.USD).isNegative()).isTrue();
    }

    @Test
    void testEquals() {
        final var amount1 = Amount.of(3L, Currencies.USD);
        final var amount2 = Amount.of(3L, Currencies.USD);
        final var amount3 = Amount.of(2L, Currencies.USD);

        assertThat(amount1).isEqualTo(amount2);
        assertThat(amount1).isNotEqualTo(amount3);
    }

    @Test
    void testEquals_with_different_currencies() {
        final var amount1 = Amount.of(3L, Currencies.USD);
        final var amount2 = Amount.of(3L, Currencies.ETH);

        assertThat(amount1).isNotEqualTo(amount2);
    }
}