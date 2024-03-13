package onlydust.com.marketplace.accounting.domain.port.in;

import onlydust.com.marketplace.accounting.domain.model.ConvertedAmount;
import onlydust.com.marketplace.accounting.domain.model.RewardId;

import java.util.Optional;

public interface RewardStatusFacadePort {
    Optional<ConvertedAmount> usdAmountOf(RewardId rewardId);
}
