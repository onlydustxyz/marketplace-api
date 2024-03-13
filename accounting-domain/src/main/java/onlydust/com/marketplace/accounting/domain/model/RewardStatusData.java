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
public class RewardStatusData {
    @NonNull
    final RewardId rewardId;

    @NonNull Boolean sponsorHasEnoughFund = false;
    ZonedDateTime unlockDate;
    ZonedDateTime invoiceReceivedAt;
    ZonedDateTime paidAt;
    @NonNull
    final Set<Network> networks = new HashSet<>();

    ConvertedAmount usdAmount;

    public Optional<ZonedDateTime> unlockDate() {
        return Optional.ofNullable(unlockDate);
    }

    public Optional<ZonedDateTime> invoiceReceivedAt() {
        return Optional.ofNullable(invoiceReceivedAt);
    }

    public Optional<ZonedDateTime> paidAt() {
        return Optional.ofNullable(paidAt);
    }

    public RewardStatusData withAdditionalNetworks(Set<Network> networks) {
        this.networks.addAll(networks);
        return this;
    }

    public RewardStatusData withAdditionalNetworks(Network... networks) {
        return withAdditionalNetworks(Set.of(networks));
    }

    public Optional<ConvertedAmount> usdAmount() {
        return Optional.ofNullable(usdAmount);
    }

    public boolean isPaid() {
        return paidAt != null;
    }
}
