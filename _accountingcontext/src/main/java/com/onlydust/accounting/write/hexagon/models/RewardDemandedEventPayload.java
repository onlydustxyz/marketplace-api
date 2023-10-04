package com.onlydust.accounting.write.hexagon.models;

import lombok.Builder;

import java.math.BigDecimal;
import java.util.UUID;

@Builder
public record RewardDemandedEventPayload(LedgerId ledgerId, RewardDemandId rewardDemandId, BigDecimal amount) {
}
