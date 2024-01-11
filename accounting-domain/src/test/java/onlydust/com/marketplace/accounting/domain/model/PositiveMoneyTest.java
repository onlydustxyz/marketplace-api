package onlydust.com.marketplace.accounting.domain.model;

import onlydust.com.marketplace.accounting.domain.stubs.Currencies;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PositiveMoneyTest {

    @Test
    void of() {
        final var amount = PositiveMoney.of(1L, Currencies.USD);
        assertThat(amount.value.longValue()).isEqualTo(1L);
        assertThat(amount.currency).isEqualTo(Currencies.USD);

        assertThatThrownBy(() -> PositiveMoney.of(-1L, Currencies.USD))
                .isInstanceOf(OnlyDustException.class)
                .hasMessage("Cannot create a positive amount with a negative value");
    }

    @Test
    void plus() {
        final var amount1 = PositiveMoney.of(1L, Currencies.USD);
        final var amount2 = PositiveMoney.of(2L, Currencies.USD);
        final var amount3 = amount1.plus(amount2);

        assertThat(amount3.value.longValue()).isEqualTo(3L);
        assertThat(amount3.currency).isEqualTo(Currencies.USD);
    }

    @Test
    void plus_with_different_currencies() {
        final var amount1 = PositiveMoney.of(1L, Currencies.USD);
        final var amount2 = PositiveMoney.of(2L, Currencies.ETH);

        assertThatThrownBy(() -> amount1.plus(amount2))
                .isInstanceOf(OnlyDustException.class)
                .hasMessage("Cannot sum different currencies");
    }
}