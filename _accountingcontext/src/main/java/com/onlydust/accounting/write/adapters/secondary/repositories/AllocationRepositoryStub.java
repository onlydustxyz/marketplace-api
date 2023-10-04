package com.onlydust.accounting.write.adapters.secondary.repositories;

import com.onlydust.accounting.write.hexagon.gateways.repositories.AllocationRepository;
import com.onlydust.accounting.write.hexagon.models.Allocation;

import java.util.ArrayList;
import java.util.List;

public class AllocationRepositoryStub implements AllocationRepository {
    private final List<Allocation> allocations = new ArrayList<>();

    public List<Allocation> allocations() {
        return allocations;
    }

    @Override
    public void save(Allocation allocation) {
        allocations.add(allocation);
    }
}
