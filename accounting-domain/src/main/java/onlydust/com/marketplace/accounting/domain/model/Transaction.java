package onlydust.com.marketplace.accounting.domain.model;

import lombok.NonNull;

public record Transaction(AccountId origin, AccountId destination, @NonNull PositiveAmount amount) {
}
