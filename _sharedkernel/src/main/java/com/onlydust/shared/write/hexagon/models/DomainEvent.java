package com.onlydust.shared.write.hexagon.models;

import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;

@EqualsAndHashCode
@ToString
public abstract class DomainEvent<T> {

    protected String type;

    protected T payload;

    protected LocalDateTime occurredOn;

    protected DomainEventStatus status;

    public DomainEvent(String type, T payload, LocalDateTime occurredOn) {
        this(type, payload, occurredOn, DomainEventStatus.NEW);
    }

    public DomainEvent(String type, T payload, LocalDateTime occurredOn, DomainEventStatus status) {
        this.type = type;
        this.payload = payload;
        this.occurredOn = occurredOn;
        this.status = status;
    }
}
