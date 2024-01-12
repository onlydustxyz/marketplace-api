package onlydust.com.marketplace.accounting.domain.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;

import java.math.BigDecimal;

@Getter
@EqualsAndHashCode(callSuper = true)
public class PositiveAmount extends Amount {

    public static final PositiveAmount ZERO = PositiveAmount.of(BigDecimal.ZERO);

    protected PositiveAmount(@NonNull BigDecimal value) {
        super(value);
        if (value.compareTo(BigDecimal.ZERO) < 0) {
            throw OnlyDustException.internalServerError("Cannot create a positive amount with a negative value");
        }
    }

    public static @NonNull PositiveAmount of(BigDecimal value) {
        return new PositiveAmount(value);
    }

    public static @NonNull PositiveAmount of(Long value) {
        return new PositiveAmount(BigDecimal.valueOf(value));
    }

    public static @NonNull PositiveAmount of(Amount amount) {
        return new PositiveAmount(amount.value);
    }

    /***
     * @param amount the positive amount to be added
     * @return a new Amount with the sum of this amount and the given amount
     */
    public @NonNull PositiveAmount add(@NonNull PositiveAmount amount) {
        return new PositiveAmount(value.add(amount.value));
    }
}
