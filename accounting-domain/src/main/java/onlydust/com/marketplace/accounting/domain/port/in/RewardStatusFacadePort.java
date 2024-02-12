package onlydust.com.marketplace.accounting.domain.port.in;

import onlydust.com.marketplace.accounting.domain.model.RewardId;

import java.math.BigDecimal;

public interface RewardStatusFacadePort {
    BigDecimal usdEquivalent(RewardId rewardId);
}
