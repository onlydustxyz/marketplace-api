package com.onlydust.accounting.write.hexagon.models;

import com.onlydust.shared.write.hexagon.models.EntityId;

import java.util.UUID;

public class RewardDemandId extends EntityId {
    public RewardDemandId(UUID id) {
        super(id);
    }

    public RewardDemandId(String id) {
        super(id);
    }
}
