package onlydust.com.marketplace.accounting.domain.view;

import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.model.Currency;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;

import java.math.BigDecimal;

import static onlydust.com.marketplace.kernel.exception.OnlyDustException.internalServerError;

/***
 * Represents a total amount with its currency and the total equivalent in dollars.
 * Unlike @{@link MoneyView}, this does not have a conversion rate as it is made out of multiple amounts with potentially different conversion rates.
 */
public record TotalMoneyView(@NonNull BigDecimal amount,
                             @NonNull Currency currency,
                             BigDecimal dollarsEquivalent) {
    public static TotalMoneyView add(TotalMoneyView left, @NonNull TotalMoneyView right) throws OnlyDustException {
        if (left != null && !left.currency().equals(right.currency())) {
            throw internalServerError("Cannot add amounts with different currencies");
        }
        
        return left == null ? right : new TotalMoneyView(
                left.amount().add(right.amount()),
                left.currency(),
                left.dollarsEquivalent().add(right.dollarsEquivalent()));
    }
}
