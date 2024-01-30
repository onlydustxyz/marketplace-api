package onlydust.com.marketplace.accounting.domain.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.*;

import java.math.BigDecimal;

@Getter
@EqualsAndHashCode
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, isGetterVisibility = JsonAutoDetect.Visibility.NONE)
@NoArgsConstructor(force = true, access = AccessLevel.PROTECTED)
public class Amount {
    public static final Amount ZERO = Amount.of(BigDecimal.ZERO);

    @NonNull
    protected final BigDecimal value;

    protected Amount(@NonNull BigDecimal value) {
        this.value = value;
    }

    public static @NonNull Amount of(BigDecimal value) {
        return new Amount(value);
    }

    public static @NonNull Amount of(Long value) {
        return new Amount(BigDecimal.valueOf(value));
    }

    /***
     * @param amount the amount to be added
     * @return a new Amount with the sum of this amount and the given amount
     */
    public @NonNull Amount add(@NonNull final Amount amount) {
        return new Amount(value.add(amount.value));
    }

    /***
     * @param amount the amount to be subtracted
     * @return a new Amount with the difference of this amount and the given amount
     */
    public @NonNull Amount subtract(@NonNull final Amount amount) {
        return add(amount.negate());
    }

    /***
     * @return a new Amount with the same value but with the opposite sign
     */
    public @NonNull Amount negate() {
        return new Amount(value.negate());
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

    /***
     * @param amount the amount to be compared
     * @return true if this amount is strictly lower than the given amount
     */
    public boolean isStrictlyLowerThan(@NonNull Amount amount) {
        return value.compareTo(amount.value) < 0;
    }

    /***
     * @param amount the amount to be compared
     * @return true if this amount is lower than or equal to the given amount
     */
    public boolean isStrictlyGreaterThan(@NonNull Amount amount) {
        return value.compareTo(amount.value) > 0;
    }

    /***
     * @param amount the amount to be compared
     * @return true if this amount is greater than or equal to the given amount
     */
    public boolean isGreaterThanOrEqual(@NonNull Amount amount) {
        return value.compareTo(amount.value) >= 0;
    }

    @Override
    public String toString() {
        return "%s".formatted(value);
    }


}
