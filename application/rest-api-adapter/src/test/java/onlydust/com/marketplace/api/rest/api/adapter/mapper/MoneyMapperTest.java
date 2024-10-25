package onlydust.com.marketplace.api.rest.api.adapter.mapper;

import onlydust.com.marketplace.accounting.domain.model.Currency;
import onlydust.com.marketplace.accounting.domain.model.Money;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.ZERO;
import static org.assertj.core.api.Assertions.assertThat;

class MoneyMapperTest {
    private final static Currency ETH = Currency.crypto("Ether", Currency.Code.ETH, 18);
    private final static Currency USD = Currency.fiat("Ether", Currency.Code.ETH, 18);

    @Test
    void should_convert_amount() {
        // Given
        final var money = Money.of(BigDecimal.valueOf(0.05), ETH);
        final var base = Money.of(BigDecimal.valueOf(123.98), USD);

        // When
        final var result = MoneyMapper.toConvertibleMoney(money, base);

        // Then
        assertThat(result.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(0.05));
        assertThat(result.getPrettyAmount()).isEqualByComparingTo(BigDecimal.valueOf(0.05));
        assertThat(result.getCurrency().getCode()).isEqualTo("ETH");
        assertThat(result.getTarget().getAmount()).isEqualByComparingTo(BigDecimal.valueOf(123.98));
        assertThat(result.getTarget().getConversionRate()).isEqualByComparingTo(BigDecimal.valueOf(2479.60));
    }

    @Test
    void should_convert_zero_amount() {
        // Given
        final var money = Money.of(0L, ETH);
        final var base = Money.of(0L, USD);

        // When
        final var result = MoneyMapper.toConvertibleMoney(money, base);

        // Then
        assertThat(result.getAmount()).isEqualByComparingTo(ZERO);
        assertThat(result.getPrettyAmount()).isEqualByComparingTo(ZERO);
        assertThat(result.getCurrency().getCode()).isEqualTo("ETH");
        assertThat(result.getTarget().getAmount()).isEqualByComparingTo(ZERO);
        assertThat(result.getTarget().getConversionRate()).isEqualByComparingTo(ONE);
    }
}