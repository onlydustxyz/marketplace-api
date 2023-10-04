package com.onlydust.shared.write.hexagon.models;

import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.UUID;

@EqualsAndHashCode
public abstract class DomainEvent<T> {

    protected UUID id;

    protected String type;

    protected T payload;

    protected LocalDateTime occurredOn;

    protected DomainEventStatus status;

    public DomainEvent(UUID id, String type, T payload, LocalDateTime occurredOn) {
        this(id, type, payload, occurredOn, DomainEventStatus.NEW);
    }

    public DomainEvent(UUID id, String type, T payload, LocalDateTime occurredOn, DomainEventStatus status) {
        this.id = id;
        this.type = type;
        this.payload = payload;
        this.occurredOn = occurredOn;
        this.status = status;
    }

    @Override
    public String toString() {
        return "RewardDemandCreatedEvent{" +
                "id=" + id +
                ", type='" + type + '\'' +
                ", payload=" + payload +
                ", occurredOn=" + occurredOn +
                '}';
    }
}
