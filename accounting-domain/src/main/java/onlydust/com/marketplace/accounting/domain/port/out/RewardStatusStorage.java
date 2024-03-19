package onlydust.com.marketplace.accounting.domain.port.out;

import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.model.RewardId;
import onlydust.com.marketplace.accounting.domain.model.RewardStatusData;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.BillingProfile;

import java.util.List;
import java.util.Optional;

public interface RewardStatusStorage {
    void save(RewardStatusData rewardStatusData);

    Optional<RewardStatusData> get(final @NonNull RewardId rewardId);

    void delete(RewardId rewardId);

    List<RewardStatusData> notPaid();

    List<RewardStatusData> notPaid(BillingProfile.Id billingProfileId);
}
