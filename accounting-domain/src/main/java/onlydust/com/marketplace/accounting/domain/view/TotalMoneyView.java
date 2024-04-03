package onlydust.com.marketplace.accounting.domain.view;

import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.model.Currency;

import java.math.BigDecimal;

/***
 * Represents a total amount with its currency and the total equivalent in dollars.
 * Unlike @{@link MoneyView}, this does not have a conversion rate as it is made out of multiple amounts with potentially different conversion rates.
 */
public record TotalMoneyView(@NonNull BigDecimal amount,
                             @NonNull Currency currency,
                             BigDecimal dollarsEquivalent) {
    public static TotalMoneyView add(TotalMoneyView left, TotalMoneyView right) {
        return left == null ? right : new TotalMoneyView(
                left.amount().add(right.amount()),
                left.currency(),
                left.dollarsEquivalent().add(right.dollarsEquivalent()));
    }
}
