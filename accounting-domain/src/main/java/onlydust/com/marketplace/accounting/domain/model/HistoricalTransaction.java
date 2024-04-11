package onlydust.com.marketplace.accounting.domain.model;

import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.view.ShortProjectView;

import java.time.ZonedDateTime;

public record HistoricalTransaction(
        @NonNull ZonedDateTime timestamp,
        @NonNull Type type,
        @NonNull SponsorAccount sponsorAccount,
        @NonNull Amount amount,
        ConvertedAmount usdAmount,
        ShortProjectView project
) {
    public enum Type {
        DEPOSIT, SPEND, ALLOWANCE, ALLOCATION
    }
}

