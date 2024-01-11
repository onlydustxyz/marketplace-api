package onlydust.com.marketplace.accounting.domain.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AmountTest {

    @Test
    void add() {
        final var amount1 = Amount.of(3L);
        final var amount2 = Amount.of(2L);

        final var result = amount1.add(amount2);
        assertThat(result).isEqualTo(Amount.of(5L));
    }

    @Test
    void subtract() {
        final var amount1 = Amount.of(3L);
        final var amount2 = Amount.of(2L);

        final var result = amount1.subtract(amount2);
        assertThat(result).isEqualTo(Amount.of(1L));
    }

    @Test
    void negate() {
        final var amount = Amount.of(3L);
        final var result = amount.negate();
        assertThat(result).isEqualTo(Amount.of(-3L));
    }

    @Test
    void isPositive() {
        assertThat(Amount.of(3L).isPositive()).isTrue();
        assertThat(Amount.of(-3L).isPositive()).isFalse();
    }

    @Test
    void isNegative() {
        assertThat(Amount.of(3L).isNegative()).isFalse();
        assertThat(Amount.of(-3L).isNegative()).isTrue();
    }

    @Test
    void isStrictlyLowerThan() {
        final var amount1 = Amount.of(3L);
        final var amount2 = Amount.of(2L);

        assertThat(amount1.isStrictlyLowerThan(amount2)).isFalse();
        assertThat(amount2.isStrictlyLowerThan(amount1)).isTrue();
        assertThat(amount1.isStrictlyLowerThan(amount1)).isFalse();
    }

    @Test
    void testEquals() {
        final var amount1 = Amount.of(3L);
        final var amount2 = Amount.of(3L);
        final var amount3 = Amount.of(2L);

        assertThat(amount1).isEqualTo(amount2);
        assertThat(amount1).isNotEqualTo(amount3);
    }
}