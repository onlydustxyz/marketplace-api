package com.onlydust.accounting.write.hexagon.models;

import com.onlydust.shared.write.hexagon.models.AggregateRoot;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
public class Ledger extends AggregateRoot<LedgerId> {

    private final ProjectId projectId;

    private final String currency;


    public Ledger(LedgerId id, ProjectId projectId, String currency) {
        super(id);
        this.projectId = projectId;
        this.currency = currency;
    }

    public RewardDemand demandReward(RewardDemandId rewardDemandId,
                                     BigDecimal amount,
                                     LocalDateTime now,
                                     BigDecimal remainingAmount) {
        if (remainingAmount.compareTo(amount) < 0) {
            throw new IllegalStateException("Not enough allocation to allow reward");
        }
        registerEvent(new RewardDemandedEvent(now, new RewardDemandedEventPayload(this.id, rewardDemandId, amount)));
        return new RewardDemand(rewardDemandId, this.id, amount);
    }

}
