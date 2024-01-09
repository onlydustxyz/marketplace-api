package onlydust.com.marketplace.accounting.domain.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;

import java.math.BigDecimal;

@Getter
@EqualsAndHashCode(callSuper = true)
public class PositiveAmount extends Amount {

    protected PositiveAmount(@NonNull BigDecimal value, @NonNull Currency currency) {
        super(value, currency);
        if (value.compareTo(BigDecimal.ZERO) < 0) {
            throw OnlyDustException.internalServerError("Cannot create a positive amount with a negative value");
        }
    }

    public static @NonNull PositiveAmount of(BigDecimal value, Currency currency) {
        return new PositiveAmount(value, currency);
    }

    public static @NonNull PositiveAmount of(Long value, Currency currency) {
        return new PositiveAmount(BigDecimal.valueOf(value), currency);
    }

    public static @NonNull PositiveAmount of(Amount amount) {
        return new PositiveAmount(amount.value, amount.currency);
    }

    /***
     * @param amount the amount to be added
     * @return a new Amount with the sum of the current value and the given amount
     * @throws OnlyDustException if the given amount has a different currency
     */
    public @NonNull PositiveAmount plus(@NonNull PositiveAmount amount) {
        if (!currency.equals(amount.currency)) {
            throw OnlyDustException.internalServerError("Cannot sum different currencies");
        }
        return new PositiveAmount(value.add(amount.value), currency);
    }
}
