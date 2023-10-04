package com.onlydust.accounting.write.hexagon.usecases.budget.allocation;

import com.onlydust.accounting.write.hexagon.models.ProjectId;

import java.math.BigDecimal;
import java.util.UUID;

public record AllocateBudgetCommand(ProjectId projectId, BigDecimal amount, String currency) {
}
