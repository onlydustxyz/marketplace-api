package onlydust.com.marketplace.accounting.domain.model;

import lombok.*;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;

import java.math.BigDecimal;

@Getter
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PositiveAmount extends Amount {

    public static final PositiveAmount ZERO = PositiveAmount.of(BigDecimal.ZERO);

    private PositiveAmount(@NonNull BigDecimal value, boolean safe) {
        super(value);
        if (safe && value.compareTo(BigDecimal.ZERO) < 0) {
            throw OnlyDustException.internalServerError("Cannot create a positive amount with a negative value");
        }
    }

    public static @NonNull PositiveAmount of(BigDecimal value) {
        return new PositiveAmount(value, true);
    }

    public static @NonNull PositiveAmount of(Long value) {
        return new PositiveAmount(BigDecimal.valueOf(value), true);
    }

    public static @NonNull PositiveAmount of(Amount amount) {
        return new PositiveAmount(amount.value, true);
    }

    /***
     * @param amount the positive amount to be added
     * @return a new Amount with the sum of this amount and the given amount
     */
    public @NonNull PositiveAmount add(@NonNull PositiveAmount amount) {
        return new PositiveAmount(value.add(amount.value), false);
    }

    public static @NonNull PositiveAmount min(PositiveAmount amount1, PositiveAmount amount2) {
        return amount1.isStrictlyLowerThan(amount2) ? amount1 : amount2;
    }
}
