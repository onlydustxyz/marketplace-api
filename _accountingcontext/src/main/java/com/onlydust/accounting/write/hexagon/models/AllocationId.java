package com.onlydust.accounting.write.hexagon.models;

import com.onlydust.shared.write.hexagon.models.EntityId;

import java.util.UUID;

public class AllocationId extends EntityId {
    public AllocationId(UUID id) {
        super(id);
    }

    public AllocationId(String id) {
        super(id);
    }
}
