package onlydust.com.marketplace.accounting.domain.model;

import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.view.ShortProjectView;

import java.time.ZonedDateTime;
import java.util.List;

public record HistoricalTransaction(
        @NonNull ZonedDateTime timestamp,
        @NonNull Type type,
        @NonNull SponsorAccount sponsorAccount,
        @NonNull Amount amount,
        ConvertedAmount usdAmount,
        ShortProjectView project
) {
    public enum Type {
        DEPOSIT, WITHDRAW, SPEND, // Balance transactions
        MINT, BURN, TRANSFER, REFUND // Allowance transactions
        ;

        public boolean isDebit() {
            return List.of(WITHDRAW, SPEND, BURN, TRANSFER).contains(this);
        }
    }
}

