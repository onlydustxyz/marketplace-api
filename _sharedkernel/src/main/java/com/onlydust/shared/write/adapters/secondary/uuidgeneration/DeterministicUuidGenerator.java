package com.onlydust.shared.write.adapters.secondary.uuidgeneration;

import com.onlydust.shared.write.hexagon.gateways.uuidgeneration.UuidGenerator;

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
