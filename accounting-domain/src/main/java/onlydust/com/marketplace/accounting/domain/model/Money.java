package onlydust.com.marketplace.accounting.domain.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;

import java.math.BigDecimal;

@Getter
@EqualsAndHashCode
public class Money {
    @NonNull
    protected final BigDecimal value;
    @NonNull
    protected final Currency currency;

    protected Money(@NonNull BigDecimal value, @NonNull Currency currency) {
        this.value = value;
        this.currency = currency;
    }

    public static @NonNull Money of(BigDecimal value, Currency currency) {
        return new Money(value, currency);
    }

    public static @NonNull Money of(Long value, Currency currency) {
        return new Money(BigDecimal.valueOf(value), currency);
    }

    /***
     * @param money the amount to be added
     * @return a new Amount with the sum of the current value and the given amount
     * @throws OnlyDustException if the given amount has a different currency
     */
    public @NonNull Money add(@NonNull final Money money) {
        if (!currency.equals(money.currency)) {
            throw OnlyDustException.internalServerError("Cannot perform arithmetic operations with different currencies");
        }
        return new Money(value.add(money.value), currency);
    }

    /***
     * @param sent the amount to be subtracted
     * @return a new Amount with the difference of the current value and the given amount
     */
    public @NonNull Money subtract(@NonNull final Money sent) {
        return add(sent.negate());
    }

    /***
     * @return a new Amount with the same value but with the opposite sign
     */
    public @NonNull Money negate() {
        return new Money(value.negate(), currency);
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

    public boolean isStrictlyLowerThan(@NonNull Money money) {
        if (!currency.equals(money.currency)) {
            throw OnlyDustException.internalServerError("Cannot compare different currencies");
        }
        return value.compareTo(money.value) < 0;
    }

    @Override
    public String toString() {
        return "%s %s".formatted(value, currency);
    }


    public Money multiply(BigDecimal bigDecimal) {
        return new Money(value.multiply(bigDecimal), currency);
    }
}
