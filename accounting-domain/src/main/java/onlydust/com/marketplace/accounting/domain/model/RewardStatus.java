package onlydust.com.marketplace.accounting.domain.model;

import lombok.Data;
import lombok.NonNull;
import lombok.experimental.Accessors;

import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.Set;

@Data
@Accessors(fluent = true, chain = true)
public class RewardStatus {
    @NonNull
    final RewardId rewardId;

    @NonNull Boolean sponsorHasEnoughFund = false;
    ZonedDateTime unlockDate;
    ZonedDateTime paymentRequestedAt;
    ZonedDateTime paidAt;
    @NonNull Set<Network> networks = Set.of();

    public Optional<ZonedDateTime> unlockDate() {
        return Optional.ofNullable(unlockDate);
    }

    public Optional<ZonedDateTime> paymentRequestedAt() {
        return Optional.ofNullable(paymentRequestedAt);
    }

    public Optional<ZonedDateTime> paidAt() {
        return Optional.ofNullable(paidAt);
    }
}
