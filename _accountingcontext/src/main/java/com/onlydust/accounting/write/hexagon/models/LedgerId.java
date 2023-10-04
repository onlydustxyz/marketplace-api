package com.onlydust.accounting.write.hexagon.models;

import com.onlydust.shared.write.hexagon.models.EntityId;

import java.util.UUID;

public class LedgerId extends EntityId {
    public LedgerId(UUID id) {
        super(id);
    }

    public LedgerId(String id) {
        super(id);
    }
}
