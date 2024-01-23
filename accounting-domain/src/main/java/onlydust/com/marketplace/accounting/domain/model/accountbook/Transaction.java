package onlydust.com.marketplace.accounting.domain.model.accountbook;

import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.model.AccountId;
import onlydust.com.marketplace.accounting.domain.model.PositiveAmount;

public record Transaction(@NonNull AccountId from, @NonNull AccountId to, @NonNull PositiveAmount amount) {
}
