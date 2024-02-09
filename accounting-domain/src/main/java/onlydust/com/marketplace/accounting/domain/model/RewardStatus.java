package onlydust.com.marketplace.accounting.domain.model;

import lombok.Data;
import lombok.NonNull;
import lombok.experimental.Accessors;

import java.time.ZonedDateTime;
import java.util.HashSet;
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
    @NonNull
    final Set<Network> networks = new HashSet<>();

    public Optional<ZonedDateTime> unlockDate() {
        return Optional.ofNullable(unlockDate);
    }

    public Optional<ZonedDateTime> paymentRequestedAt() {
        return Optional.ofNullable(paymentRequestedAt);
    }

    public Optional<ZonedDateTime> paidAt() {
        return Optional.ofNullable(paidAt);
    }

    public RewardStatus withAdditionalNetworks(Set<Network> networks) {
        this.networks.addAll(networks);
        return this;
    }

    public RewardStatus withAdditionalNetworks(Network... networks) {
        return withAdditionalNetworks(Set.of(networks));
    }
}
