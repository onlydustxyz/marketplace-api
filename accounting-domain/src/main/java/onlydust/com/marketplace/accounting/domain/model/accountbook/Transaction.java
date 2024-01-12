package onlydust.com.marketplace.accounting.domain.model.accountbook;

import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.model.Account;
import onlydust.com.marketplace.accounting.domain.model.PositiveAmount;

public record Transaction(@NonNull Account.Id from, @NonNull Account.Id to, @NonNull PositiveAmount amount) {
}
