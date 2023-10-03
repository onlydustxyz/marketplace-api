package com.onlydust.projectmanagement.adapters.secondary.uuidgeneration;

import com.onlydust.projectmanagement.hexagon.gateways.uuidgeneration.UuidGenerator;

import java.util.UUID;

public class DeterministicUuidGenerator implements UuidGenerator {

    private UUID nextUuid;

    @Override
    public UUID generate() {
        return nextUuid;
    }

    public void setNextUuid(UUID uuid) {
        this.nextUuid = uuid;
    }
}
