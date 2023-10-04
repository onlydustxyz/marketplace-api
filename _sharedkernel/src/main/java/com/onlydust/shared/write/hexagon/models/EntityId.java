package com.onlydust.shared.write.hexagon.models;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.UUID;

@ToString
@EqualsAndHashCode
@Getter
public abstract class EntityId {

    protected UUID id;

    public EntityId(UUID id) {
        this.id = id;
    }

    public EntityId(String id) {
        this.id = UUID.fromString(id);
    }

}
