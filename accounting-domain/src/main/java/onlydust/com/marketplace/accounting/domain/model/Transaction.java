package onlydust.com.marketplace.accounting.domain.model;

import lombok.NonNull;

public record Transaction(@NonNull Account.Id origin, @NonNull Account.Id destination, @NonNull PositiveAmount amount) {
}
