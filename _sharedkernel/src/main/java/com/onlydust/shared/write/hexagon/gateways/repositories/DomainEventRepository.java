package com.onlydust.shared.write.hexagon.gateways.repositories;


import com.onlydust.shared.write.hexagon.models.DomainEvent;

public interface DomainEventRepository {

    void save(DomainEvent<?> domainEvent);
}
