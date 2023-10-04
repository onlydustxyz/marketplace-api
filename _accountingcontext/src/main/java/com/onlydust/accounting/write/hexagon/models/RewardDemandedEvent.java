package com.onlydust.accounting.write.hexagon.models;

import com.onlydust.shared.write.hexagon.models.DomainEvent;
import com.onlydust.shared.write.hexagon.models.DomainEventStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public class RewardDemandedEvent extends DomainEvent<RewardDemandedEventPayload> {

    public RewardDemandedEvent(UUID id, LocalDateTime occurredOn, RewardDemandedEventPayload payload) {
        super(id, "REWARD_DEMANDED", payload, occurredOn);
    }

    public RewardDemandedEvent(UUID id, LocalDateTime occurredOn, RewardDemandedEventPayload payload, DomainEventStatus status) {
        super(id, "REWARD_DEMANDED", payload, occurredOn, status);
    }
}
