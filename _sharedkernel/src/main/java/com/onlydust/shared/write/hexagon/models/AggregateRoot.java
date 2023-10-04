package com.onlydust.shared.write.hexagon.models;

import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
public abstract class AggregateRoot extends Entity {

    @EqualsAndHashCode.Exclude
    private final List<DomainEvent<?>> domainEvents = new ArrayList<>();

    public AggregateRoot(UUID id) {
        super(id);
    }

    protected void registerEvent(DomainEvent<?> event) {
        domainEvents.add(event);
    }

    public List<DomainEvent<?>> getRegisteredDomainEvents() {
        return Collections.unmodifiableList(domainEvents);
    }
}
