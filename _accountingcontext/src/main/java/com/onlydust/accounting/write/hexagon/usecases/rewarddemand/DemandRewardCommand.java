package com.onlydust.accounting.write.hexagon.usecases.rewarddemand;

import java.math.BigDecimal;
import java.util.UUID;

public record DemandRewardCommand(UUID ledgerId, BigDecimal amount){}
