package onlydust.com.marketplace.accounting.domain.model;

import lombok.Data;
import lombok.NonNull;
import lombok.experimental.Accessors;

import java.time.ZonedDateTime;
import java.util.Set;

@Data
@Accessors(fluent = true, chain = true)
public class RewardStatus {
    @NonNull
    final RewardId rewardId;

    Boolean sponsorHasEnoughFund;
    ZonedDateTime unlockDate;
    Boolean paymentRequestedAt;
    Boolean paidAt;
    Set<Network> networks;
}
