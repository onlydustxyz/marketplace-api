package onlydust.com.marketplace.accounting.domain.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@EqualsAndHashCode
public class Amount {
    private final BigDecimal value;

    private Amount(BigDecimal value) {
        this.value = value;
    }

    public static Amount of(BigDecimal value) {
        return new Amount(value);
    }

    public Amount plus(Amount amount) {
        return new Amount(value.add(amount.value));
    }

    public boolean isPositive() {
        return value.compareTo(BigDecimal.ZERO) >= 0;
    }

    public boolean isNegative() {
        return !isPositive();
    }
}
