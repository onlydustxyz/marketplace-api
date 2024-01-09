package onlydust.com.marketplace.accounting.domain.model;

import lombok.NonNull;

public record Transaction(Account.Id origin, Account.Id destination, @NonNull PositiveAmount amount) {
}
