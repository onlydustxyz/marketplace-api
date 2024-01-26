package onlydust.com.marketplace.accounting.domain.model.accountbook;

import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.model.Ledger;
import onlydust.com.marketplace.accounting.domain.model.PositiveAmount;

public record Transaction(@NonNull Ledger.Id from, @NonNull Ledger.Id to, @NonNull PositiveAmount amount) {
}
