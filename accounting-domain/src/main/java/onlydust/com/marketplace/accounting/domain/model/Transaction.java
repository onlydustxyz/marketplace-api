package onlydust.com.marketplace.accounting.domain.model;

import lombok.NonNull;

public record Transaction(@NonNull Ledger.Id origin, @NonNull Ledger.Id destination, @NonNull PositiveMoney amount) {
}
