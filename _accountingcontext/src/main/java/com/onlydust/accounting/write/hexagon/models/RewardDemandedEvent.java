package com.onlydust.accounting.write.hexagon.models;

import com.onlydust.shared.write.hexagon.models.DomainEvent;
import com.onlydust.shared.write.hexagon.models.DomainEventStatus;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
public class RewardDemandedEvent extends DomainEvent<RewardDemandedEventPayload> {

    public RewardDemandedEvent(LocalDateTime occurredOn, RewardDemandedEventPayload payload) {
        super("REWARD_DEMANDED", payload, occurredOn);
    }

    public RewardDemandedEvent(LocalDateTime occurredOn, RewardDemandedEventPayload payload, DomainEventStatus status) {
        super("REWARD_DEMANDED", payload, occurredOn, status);
    }
}
