package com.onlydust.accounting.write.hexagon.models;

import com.onlydust.shared.write.hexagon.models.EntityId;

import java.util.UUID;

public class ProjectId extends EntityId {
    public ProjectId(UUID id) {
        super(id);
    }

    public ProjectId(String id) {
        super(id);
    }
}
