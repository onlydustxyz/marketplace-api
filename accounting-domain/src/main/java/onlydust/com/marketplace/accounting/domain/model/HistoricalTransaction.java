package onlydust.com.marketplace.accounting.domain.model;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.view.ProjectShortView;

import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public record HistoricalTransaction(
        @NonNull UUID id,
        @NonNull ZonedDateTime timestamp,
        @NonNull Type type,
        @NonNull SponsorAccount sponsorAccount,
        @NonNull Amount amount,
        ConvertedAmount usdAmount,
        ProjectShortView project
) {
    public enum Type {
        DEPOSIT, WITHDRAW, SPEND, // Balance transactions
        MINT, BURN, TRANSFER, REFUND // Allowance transactions
        ;

        public boolean isDebit() {
            return List.of(WITHDRAW, SPEND, BURN, TRANSFER).contains(this);
        }
    }

    @Data
    @Builder
    public static class Filters {
        @Builder.Default
        List<Currency.Id> currencies = List.of();
        @Builder.Default
        List<ProjectId> projectIds = List.of();
        @Builder.Default
        List<Type> types = List.of();
        Date from;
        Date to;
    }

    public enum Sort {
        DATE, TYPE, AMOUNT, PROJECT
    }
}

