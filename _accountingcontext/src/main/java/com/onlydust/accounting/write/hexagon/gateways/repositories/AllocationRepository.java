package com.onlydust.accounting.write.hexagon.gateways.repositories;

import com.onlydust.accounting.write.hexagon.models.Allocation;
import com.onlydust.accounting.write.hexagon.models.Ledger;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

public interface AllocationRepository {
    void save(Allocation allocation);
}
