package com.onlydust.shared.write.adapters.secondary.repositories;

import com.onlydust.shared.write.hexagon.gateways.repositories.DomainEventRepository;
import com.onlydust.shared.write.hexagon.models.DomainEvent;

import java.util.ArrayList;
import java.util.List;

public class DomainEventRepositoryStub implements DomainEventRepository {

    private final List<DomainEvent<?>> domainEvents = new ArrayList<>();

    @Override
    public void save(DomainEvent<?> domainEvent) {
        domainEvents.add(domainEvent);
    }

    public List<DomainEvent<?>> domainEvents() {
        return domainEvents;
    }
}
