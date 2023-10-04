package com.onlydust.accounting.write.hexagon.usecases.budget.allocation;

import com.onlydust.accounting.write.hexagon.gateways.repositories.AllocationRepository;
import com.onlydust.accounting.write.hexagon.models.Allocation;
import com.onlydust.shared.write.adapters.secondary.uuidgeneration.DeterministicUuidGenerator;
import com.onlydust.shared.write.hexagon.gateways.uuidgeneration.UuidGenerator;

public class AllocateBudgetCommandHandler {
    private final UuidGenerator uuidGenerator;
    private final AllocationRepository allocationRepository;

    public AllocateBudgetCommandHandler(UuidGenerator uuidGenerator, AllocationRepository allocationRepository) {
        this.uuidGenerator = uuidGenerator;
        this.allocationRepository = allocationRepository;
    }

     public void handle(AllocateBudgetCommand command) {
        final var allocation = new Allocation(uuidGenerator.generate(), command.ledgerId(), command.amount());
        allocationRepository.save(allocation);
     }

}
