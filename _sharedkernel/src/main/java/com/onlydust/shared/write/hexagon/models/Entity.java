package com.onlydust.shared.write.hexagon.models;


import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.UUID;

@EqualsAndHashCode
@Getter
public abstract class Entity<T extends EntityId> {

    protected final T id;

    public Entity(T id) {
        this.id = id;
    }
}
