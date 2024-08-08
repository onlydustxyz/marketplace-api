package onlydust.com.marketplace.api.read.utils;

import lombok.NonNull;
import onlydust.com.backoffice.api.contract.model.TotalMoneyWithUsdEquivalentResponse;

public interface Arithmetic {
    static TotalMoneyWithUsdEquivalentResponse sum(final TotalMoneyWithUsdEquivalentResponse left, @NonNull final TotalMoneyWithUsdEquivalentResponse right) {
        return left == null ? right : new TotalMoneyWithUsdEquivalentResponse()
                .currency(left.getCurrency())
                .amount(left.getAmount().add(right.getAmount()))
                .dollarsEquivalent(left.getDollarsEquivalent().add(right.getDollarsEquivalent()));
    }
}
