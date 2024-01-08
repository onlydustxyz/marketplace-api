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

    public static @NonNull Amount of(BigDecimal value, Currency currency) {
        return new Amount(value, currency);
    }

    public static @NonNull Amount of(Long value, Currency currency) {
        return new Amount(BigDecimal.valueOf(value), currency);
    }

    /***
     * @param amount the amount to be added
     * @return a new Amount with the sum of the current value and the given amount
     * @throws OnlyDustException if the given amount has a different currency
     */
    public @NonNull Amount plus(@NonNull Amount amount) {
        if (!currency.equals(amount.currency)) {
            throw OnlyDustException.internalServerError("Cannot sum different currencies");
        }
        return new Amount(value.add(amount.value), currency);
    }

    /***
     * @return a new Amount with the same value but with the opposite sign
     */
    public @NonNull Amount negate() {
        return new Amount(value.negate(), currency);
    }

    /***
     * @return true if the value is greater or equal to zero
     */
    public boolean isPositive() {
        return value.compareTo(BigDecimal.ZERO) >= 0;
    }

    /***
     * @return true if the value is strictly less than zero
     */
    public boolean isNegative() {
        return !isPositive();
    }

    @Override
    public String toString() {
        return "%s%s".formatted(value, currency);
    }
}
