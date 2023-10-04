package com.onlydust.accounting.write.hexagon.usecases.budget.allocation;

import com.onlydust.accounting.write.hexagon.gateways.repositories.AllocationRepository;
import com.onlydust.accounting.write.hexagon.gateways.repositories.LedgerRepository;
import com.onlydust.accounting.write.hexagon.models.Allocation;
import com.onlydust.accounting.write.hexagon.models.Ledger;
import com.onlydust.shared.write.hexagon.gateways.uuidgeneration.UuidGenerator;

public class AllocateBudgetCommandHandler {
    private final UuidGenerator uuidGenerator;
    private final AllocationRepository allocationRepository;
    private final LedgerRepository ledgerRepository;

    public AllocateBudgetCommandHandler(UuidGenerator uuidGenerator, AllocationRepository allocationRepository, LedgerRepository ledgerRepository) {
        this.uuidGenerator = uuidGenerator;
        this.allocationRepository = allocationRepository;
        this.ledgerRepository = ledgerRepository;
    }

    public void handle(AllocateBudgetCommand command) {
        final var ledger =
                ledgerRepository.byProjectIdAndCurrency(command.projectId(), command.currency())
                        .orElseGet(() -> {
                            final var newLedger = new Ledger(uuidGenerator.generate(), command.projectId(), command.currency());
                            ledgerRepository.save(newLedger);
                            return newLedger;
                        });
        final var allocation = new Allocation(uuidGenerator.generate(), ledger.getId(), command.amount());
        allocationRepository.save(allocation);
    }

}
