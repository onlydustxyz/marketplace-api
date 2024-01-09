package onlydust.com.marketplace.accounting.domain.model;

import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PositiveAmountTest {

    @Test
    void of() {
        final var amount = PositiveAmount.of(1L, Currency.Usd);
        assertThat(amount.value.longValue()).isEqualTo(1L);
        assertThat(amount.currency).isEqualTo(Currency.Usd);

        assertThatThrownBy(() -> PositiveAmount.of(-1L, Currency.Usd))
                .isInstanceOf(OnlyDustException.class)
                .hasMessage("Cannot create a positive amount with a negative value");
    }

    @Test
    void plus() {
        final var amount1 = PositiveAmount.of(1L, Currency.Usd);
        final var amount2 = PositiveAmount.of(2L, Currency.Usd);
        final var amount3 = amount1.plus(amount2);

        assertThat(amount3.value.longValue()).isEqualTo(3L);
        assertThat(amount3.currency).isEqualTo(Currency.Usd);
    }

    @Test
    void plus_with_different_currencies() {
        final var amount1 = PositiveAmount.of(1L, Currency.Usd);
        final var amount2 = PositiveAmount.of(2L, Currency.Eth);

        assertThatThrownBy(() -> amount1.plus(amount2))
                .isInstanceOf(OnlyDustException.class)
                .hasMessage("Cannot sum different currencies");
    }
}