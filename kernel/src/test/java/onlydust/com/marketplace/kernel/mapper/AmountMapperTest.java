package onlydust.com.marketplace.kernel.mapper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static onlydust.com.marketplace.kernel.mapper.AmountMapper.pretty;
import static org.assertj.core.api.Assertions.assertThat;

class AmountMapperTest {
    @ParameterizedTest
    @ValueSource(doubles = {0.000000001, 0.00000001, 0.0000001, 0.000001, 0.00001, 0.0001, 0.001, 0.01, 0.1, 1, 10, 100, 1000, 10000, 100000, 1000000})
    void should_have_same_value_at_usd_penny_level(Double rate) {
        final var price = BigDecimal.valueOf(100.123456789012345678);
        final var pretty = pretty(price, 18, BigDecimal.valueOf(rate));
        final var usdPrice = price.multiply(BigDecimal.valueOf(rate));
        final var usdPretty = pretty.multiply(BigDecimal.valueOf(rate));

        assertThat(usdPretty.subtract(usdPrice).abs().setScale(2, RoundingMode.HALF_EVEN)).isLessThanOrEqualTo(BigDecimal.valueOf(0.01));
    }

    @Test
    void should_prettify_amount_based_on_usd_rate() {
        // 1:1 USD rate
        assertThat(pretty(BigDecimal.valueOf(100.123456789012345678), 18, BigDecimal.valueOf(1.01))).isEqualTo(BigDecimal.valueOf(100.12));
        assertThat(pretty(BigDecimal.valueOf(100.126456789012345678), 18, BigDecimal.valueOf(1.01))).isEqualTo(BigDecimal.valueOf(100.13));

        // Small USD rate
        assertThat(pretty(BigDecimal.valueOf(100.123456789012345678), 18, BigDecimal.valueOf(0.002356))).isEqualTo(BigDecimal.valueOf(100));
        assertThat(pretty(BigDecimal.valueOf(100.723456789012345678), 18, BigDecimal.valueOf(0.002356))).isEqualTo(BigDecimal.valueOf(101));

        // Big USD rate
        assertThat(pretty(BigDecimal.valueOf(100.123456789012345678), 18, BigDecimal.valueOf(63336.80))).isEqualTo(BigDecimal.valueOf(100.1234568));
        assertThat(pretty(BigDecimal.valueOf(100.123456789012345678), 18, BigDecimal.valueOf(63336.80))).isEqualTo(BigDecimal.valueOf(100.1234568));

        // No USD rate
        assertThat(pretty(BigDecimal.valueOf(100.123456789012345678), 18, null)).isEqualTo(BigDecimal.valueOf(100.123456789012345678));
        assertThat(pretty(BigDecimal.valueOf(100.123456789012345678), 6, null)).isEqualTo(BigDecimal.valueOf(100.123457));
    }

    @Test
    void should_not_prettify_amount_if_already_pretty() {
        // 1:1 USD rate
        assertThat(pretty(BigDecimal.valueOf(100), 18, BigDecimal.valueOf(1.01))).isEqualTo(BigDecimal.valueOf(100));

        // Small USD rate
        assertThat(pretty(BigDecimal.valueOf(100), 18, BigDecimal.valueOf(0.002356))).isEqualTo(BigDecimal.valueOf(100));

        // Big USD rate
        assertThat(pretty(BigDecimal.valueOf(100.12), 18, BigDecimal.valueOf(65987))).isEqualTo(BigDecimal.valueOf(100.12));

        // No USD rate
        assertThat(pretty(BigDecimal.valueOf(100.12), 18, null)).isEqualTo(BigDecimal.valueOf(100.12));
        assertThat(pretty(BigDecimal.valueOf(100.12), 6, null)).isEqualTo(BigDecimal.valueOf(100.12));
    }
}
