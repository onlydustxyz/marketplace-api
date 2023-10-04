package com.onlydust.accounting.write.hexagon.models;

import com.onlydust.shared.write.hexagon.models.AggregateRoot;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class Ledger extends AggregateRoot {

    private final UUID projectId;

    private final String currency;


    public Ledger(UUID id, UUID projectId, String currency) {
        super(id);
        this.projectId = projectId;
        this.currency = currency;
    }

    public RewardDemand demandReward(UUID rewardDemandId, BigDecimal amount, LocalDateTime now, BigDecimal remainingAmount) {
        if (remainingAmount.compareTo(amount) < 0) {
            throw new IllegalStateException("Not enough allocation to allow reward");
        }
        registerEvent(new RewardDemandedEvent(rewardDemandId, now, new RewardDemandedEventPayload(this.id, amount)));
        return new RewardDemand(rewardDemandId, this.id, amount);
    }

}
