package com.onlydust.shared.write.hexagon.models;

import java.util.UUID;

public abstract class EntityId {

    protected UUID id;

    public EntityId(UUID id) {
        this.id = id;
    }

    public EntityId(String id) {
        this.id = UUID.fromString(id);
    }

    public UUID getId() {
        return id;
    }
}
