package onlydust.com.marketplace.accounting.domain.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;

import java.math.BigDecimal;

@Getter
@EqualsAndHashCode(callSuper = true)
public class PositiveMoney extends Money {

    protected PositiveMoney(@NonNull BigDecimal value, @NonNull Currency currency) {
        super(value, currency);
        if (value.compareTo(BigDecimal.ZERO) < 0) {
            throw OnlyDustException.internalServerError("Cannot create a positive amount with a negative value");
        }
    }

    public static @NonNull PositiveMoney of(BigDecimal value, Currency currency) {
        return new PositiveMoney(value, currency);
    }

    public static @NonNull PositiveMoney of(Long value, Currency currency) {
        return new PositiveMoney(BigDecimal.valueOf(value), currency);
    }

    public static @NonNull PositiveMoney of(Money money) {
        return new PositiveMoney(money.value, money.currency);
    }

    /***
     * @param amount the amount to be added
     * @return a new Amount with the sum of the current value and the given amount
     * @throws OnlyDustException if the given amount has a different currency
     */
    public @NonNull PositiveMoney plus(@NonNull PositiveMoney amount) {
        if (!currency.equals(amount.currency)) {
            throw OnlyDustException.internalServerError("Cannot sum different currencies");
        }
        return new PositiveMoney(value.add(amount.value), currency);
    }
}
