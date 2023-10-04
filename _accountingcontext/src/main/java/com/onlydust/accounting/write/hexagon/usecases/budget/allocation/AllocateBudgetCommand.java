package com.onlydust.accounting.write.hexagon.usecases.budget.allocation;

import java.math.BigDecimal;
import java.util.UUID;

public record AllocateBudgetCommand(UUID projectId, BigDecimal amount, String currency) {
}
