package onlydust.com.marketplace.accounting.domain.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;

import java.math.BigDecimal;

@Getter
@EqualsAndHashCode
public class Amount {
    @NonNull
    private final BigDecimal value;
    @NonNull
    private final Currency currency;

    private Amount(@NonNull BigDecimal value, @NonNull Currency currency) {
        this.value = value;
        this.currency = currency;
    }

    public static Amount of(BigDecimal value, Currency currency) {
        return new Amount(value, currency);
    }

    public static Amount of(Long value, Currency currency) {
        return new Amount(BigDecimal.valueOf(value), currency);
    }

    public Amount plus(@NonNull Amount amount) {
        if (!currency.equals(amount.currency)) {
            throw OnlyDustException.internalServerError("Cannot sum different currencies");
        }
        return new Amount(value.add(amount.value), currency);
    }

    public boolean isPositive() {
        return value.compareTo(BigDecimal.ZERO) >= 0;
    }

    public boolean isNegative() {
        return !isPositive();
    }
}
