package onlydust.com.marketplace.accounting.domain.port.out;

import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.model.RewardId;
import onlydust.com.marketplace.accounting.domain.model.RewardStatus;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.BillingProfile;

import java.util.List;
import java.util.Optional;

public interface RewardStatusStorage {
    void save(RewardStatus rewardStatus);

    Optional<RewardStatus> get(final @NonNull RewardId rewardId);

    void delete(RewardId rewardId);

    List<RewardStatus> notPaid();

    List<RewardStatus> notPaid(BillingProfile.Id userId);
}
