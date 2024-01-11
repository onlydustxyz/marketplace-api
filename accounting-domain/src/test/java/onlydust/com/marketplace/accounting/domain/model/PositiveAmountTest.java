package onlydust.com.marketplace.accounting.domain.model;

import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PositiveAmountTest {

    @Test
    void of() {
        final var amount = PositiveAmount.of(1L);
        assertThat(amount.value.longValue()).isEqualTo(1L);

        assertThatThrownBy(() -> PositiveAmount.of(-1L))
                .isInstanceOf(OnlyDustException.class)
                .hasMessage("Cannot create a positive amount with a negative value");
    }

    @Test
    void plus() {
        final var amount1 = PositiveAmount.of(1L);
        final var amount2 = PositiveAmount.of(2L);
        final var amount3 = amount1.add(amount2);

        assertThat(amount3.value.longValue()).isEqualTo(3L);
    }
}