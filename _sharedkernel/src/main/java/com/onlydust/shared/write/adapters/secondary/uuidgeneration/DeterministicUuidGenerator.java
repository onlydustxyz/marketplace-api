package com.onlydust.shared.write.adapters.secondary.uuidgeneration;

import com.onlydust.shared.write.hexagon.gateways.uuidgeneration.UuidGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DeterministicUuidGenerator implements UuidGenerator {

    private final List<UUID> nextUuids = new ArrayList<>();

    @Override
    public UUID generate() {
        return nextUuids.remove(0);
    }

    public void addNextUuid(UUID uuid) {
        this.nextUuids.add(uuid);
    }
}
