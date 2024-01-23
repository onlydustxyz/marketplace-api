package onlydust.com.marketplace.accounting.domain.model;

import lombok.NonNull;

public record Transaction(@NonNull AccountId origin, @NonNull AccountId destination, @NonNull PositiveMoney amount) {
}
