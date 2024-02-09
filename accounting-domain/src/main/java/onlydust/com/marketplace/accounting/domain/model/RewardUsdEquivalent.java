package onlydust.com.marketplace.accounting.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

@Getter
@Accessors(fluent = true)
@AllArgsConstructor
public class RewardUsdEquivalent {
    @NonNull RewardId rewardId;
    @NonNull ZonedDateTime rewardCreatedAt;
    @NonNull Currency.Id rewardCurrencyId;
    ZonedDateTime kycbVerifiedAt;
    ZonedDateTime currencyQuoteAvailableAt;
    ZonedDateTime unlockDate;
    @NonNull BigDecimal rewardAmount;

    public Optional<ZonedDateTime> equivalenceSealingDate() {
        if (kycNotVerified() || notLiquid() || locked()) return Optional.empty();
        return max(rewardCreatedAt, kycbVerifiedAt, currencyQuoteAvailableAt, unlockDate);
    }

    private Optional<ZonedDateTime> max(ZonedDateTime... dates) {
        return Arrays.stream(dates).filter(Objects::nonNull).max(ZonedDateTime::compareTo);
    }

    private boolean locked() {
        return unlockDate != null && unlockDate.isAfter(ZonedDateTime.now());
    }

    private boolean notLiquid() {
        return currencyQuoteAvailableAt == null;
    }

    private boolean kycNotVerified() {
        return kycbVerifiedAt == null;
    }
}
