package com.onlydust.accounting.write.hexagon.usecases.rewarddemand;

import com.onlydust.accounting.write.hexagon.models.LedgerId;

import java.math.BigDecimal;
import java.util.UUID;

public record DemandRewardCommand(LedgerId ledgerId, BigDecimal amount) {
}
