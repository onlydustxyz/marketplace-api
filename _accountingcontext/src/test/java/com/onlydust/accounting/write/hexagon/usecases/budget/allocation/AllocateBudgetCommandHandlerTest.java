package com.onlydust.accounting.write.hexagon.usecases.budget.allocation;

import com.onlydust.accounting.write.adapters.secondary.repositories.AllocationRepositoryStub;
import com.onlydust.accounting.write.hexagon.models.Allocation;
import com.onlydust.shared.write.adapters.secondary.uuidgeneration.DeterministicUuidGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class AllocateBudgetCommandHandlerTest {

    private final UUID allocationId = UUID.fromString("437ac7bd-23c2-42e3-a906-2e61965326d9");
    private final UUID ledgerId = UUID.fromString("ddc420a3-3cc9-414c-9f0b-0f9af6770c6b");
    private final AllocationRepositoryStub allocationRepository = new AllocationRepositoryStub();

    private final DeterministicUuidGenerator uuidGenerator = new DeterministicUuidGenerator();

    @BeforeEach
    void setup() {
        uuidGenerator.setNextUuid(allocationId);
    }

    @Test
    void should_allocate_budget_to_an_existing_ledger() {
        new AllocateBudgetCommandHandler(uuidGenerator, allocationRepository).handle(new AllocateBudgetCommand(ledgerId, BigDecimal.valueOf(15000)));

        assertExistingAllocations(new Allocation(allocationId, ledgerId, BigDecimal.valueOf(15000)));
    }

    private void assertExistingAllocations(Allocation... allocations) {
        assertThat(allocationRepository.allocations()).containsExactly(allocations);
    }
}
