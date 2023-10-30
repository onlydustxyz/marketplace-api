package onlydust.com.marketplace.api.domain.service;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.domain.exception.OnlyDustException;
import onlydust.com.marketplace.api.domain.model.RequestRewardCommand;
import onlydust.com.marketplace.api.domain.port.input.RewardFacadePort;
import onlydust.com.marketplace.api.domain.port.output.ProjectStoragePort;
import onlydust.com.marketplace.api.domain.port.output.RewardStoragePort;

import java.util.UUID;

@AllArgsConstructor
public class RewardService<Authentication> implements RewardFacadePort<Authentication> {

    private final RewardStoragePort<Authentication> rewardStoragePort;
    private final ProjectStoragePort projectStoragePort;
    private final PermissionService permissionService;

    @Override
    public UUID requestPayment(Authentication authentication, UUID projectLeadId,
                               RequestRewardCommand command) {
        if (!permissionService.isUserProjectLead(command.getProjectId(), projectLeadId)) {
            throw OnlyDustException.forbidden("User must be project lead to request a reward");
        }

        final var budgets = projectStoragePort.findBudgets(command.getProjectId()).getBudgets();
        if (budgets.stream().noneMatch(budget -> budget.getCurrency() == command.getCurrency() &&
                                                 budget.getRemaining().compareTo(command.getAmount()) >= 0)) {
            throw OnlyDustException.badRequest(("Not enough budget of currency %s for project %s to request a " +
                                                "reward with an amount of %s")
                    .formatted(command.getCurrency(), command.getProjectId(), command.getAmount()));
        }

        return rewardStoragePort.requestPayment(authentication, command);
    }

    @Override
    public void cancelPayment(Authentication authentication, UUID projectLeadId, UUID projectId, UUID rewardId) {
        if (permissionService.isUserProjectLead(projectId, projectLeadId)) {
            rewardStoragePort.cancelPayment(authentication, rewardId);
        } else {
            throw OnlyDustException.forbidden("User must be project lead to cancel a reward");
        }
    }
}
