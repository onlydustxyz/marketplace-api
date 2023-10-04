package com.onlydust.accounting.write.hexagon.usecases.budget.allocation;

import com.onlydust.accounting.write.adapters.secondary.repositories.AllocationRepositoryStub;
import com.onlydust.accounting.write.adapters.secondary.repositories.LedgerRepositoryStub;
import com.onlydust.accounting.write.hexagon.models.Allocation;
import com.onlydust.accounting.write.hexagon.models.Ledger;
import com.onlydust.shared.write.adapters.secondary.uuidgeneration.DeterministicUuidGenerator;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class AllocateBudgetCommandHandlerTest {

    private final UUID allocationId = UUID.fromString("437ac7bd-23c2-42e3-a906-2e61965326d9");
    private final UUID projectId = UUID.fromString("ddc420a3-3cc9-414c-9f0b-0f9af6770c6b");
    private final UUID ledgerId = UUID.fromString("ddc420a3-3cc9-414c-9f0b-0f9af6770c6b");
    private final AllocationRepositoryStub allocationRepository = new AllocationRepositoryStub();
    private final LedgerRepositoryStub ledgerRepository = new LedgerRepositoryStub();
    private final DeterministicUuidGenerator uuidGenerator = new DeterministicUuidGenerator();

    @Test
    void should_allocate_budget_to_an_existing_ledger() {
        ledgerRepository.save(new Ledger(ledgerId, projectId, "USD"));

        uuidGenerator.addNextUuid(allocationId);
        new AllocateBudgetCommandHandler(uuidGenerator, allocationRepository, ledgerRepository)
                .handle(new AllocateBudgetCommand(projectId, BigDecimal.valueOf(15000), "USD"));

        assertExistingAllocations(new Allocation(allocationId, projectId, BigDecimal.valueOf(15000)));
    }

    @Test
    void should_create_ledger_upon_first_budget_allocation() {
        uuidGenerator.addNextUuid(ledgerId);
        uuidGenerator.addNextUuid(allocationId);

        new AllocateBudgetCommandHandler(uuidGenerator, allocationRepository, ledgerRepository)
                .handle(new AllocateBudgetCommand(projectId, BigDecimal.valueOf(15000), "USD"));

        assertExistingAllocations(new Allocation(allocationId, projectId, BigDecimal.valueOf(15000)));
        assertExisingLedgers(new Ledger(ledgerId, projectId, "USD"));
    }

    private void assertExistingAllocations(Allocation... allocations) {
        assertThat(allocationRepository.allocations()).containsExactly(allocations);
    }

    private void assertExisingLedgers(Ledger... ledgers) {
        assertThat(ledgerRepository.ledgers()).containsExactly(ledgers);
    }
}
