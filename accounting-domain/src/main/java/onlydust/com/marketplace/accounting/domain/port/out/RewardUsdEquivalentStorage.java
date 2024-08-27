package onlydust.com.marketplace.accounting.domain.port.out;

import onlydust.com.marketplace.kernel.model.RewardId;
import onlydust.com.marketplace.accounting.domain.model.RewardUsdEquivalent;

import java.util.Optional;

public interface RewardUsdEquivalentStorage {
    Optional<RewardUsdEquivalent> get(RewardId rewardId);
}
