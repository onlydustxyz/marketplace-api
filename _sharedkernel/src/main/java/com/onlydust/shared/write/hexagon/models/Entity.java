package com.onlydust.shared.write.hexagon.models;


import lombok.EqualsAndHashCode;

import java.util.UUID;

@EqualsAndHashCode
public abstract class Entity {

    protected final UUID id;

    public Entity(UUID id) {
        this.id = id;
    }
    public UUID getId() {
        return id;
    }
}
