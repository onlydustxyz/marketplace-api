package onlydust.com.marketplace.accounting.domain.port.out;

import onlydust.com.marketplace.accounting.domain.model.RewardId;
import onlydust.com.marketplace.accounting.domain.model.RewardStatus;

import java.util.Optional;

public interface RewardStatusStorage {
    void save(RewardStatus rewardStatus);

    Optional<RewardStatus> get(RewardId rewardId);
}
