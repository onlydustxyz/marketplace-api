package onlydust.com.marketplace.api.domain.service;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import onlydust.com.marketplace.api.domain.model.RequestRewardCommand;
import onlydust.com.marketplace.api.domain.port.input.RewardFacadePort;
import onlydust.com.marketplace.api.domain.port.output.IndexerPort;
import onlydust.com.marketplace.api.domain.port.output.ProjectStoragePort;
import onlydust.com.marketplace.api.domain.port.output.RewardServicePort;
import onlydust.com.marketplace.api.domain.port.output.UserStoragePort;
import onlydust.com.marketplace.api.domain.view.UserRewardView;

import java.math.BigDecimal;
import java.util.UUID;

@AllArgsConstructor
public class RewardService implements RewardFacadePort {

    private final RewardServicePort rewardServicePort;
    private final ProjectStoragePort projectStoragePort;
    private final PermissionService permissionService;
    private final IndexerPort indexerPort;
    private final UserStoragePort userStoragePort;

    @Override
    public UUID requestPayment(UUID projectLeadId,
                               RequestRewardCommand command) {
        if (!permissionService.isUserProjectLead(command.getProjectId(), projectLeadId)) {
            throw OnlyDustException.forbidden("User must be project lead to request a reward");
        }
        if (command.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw OnlyDustException.forbidden("Amount must be greater than 0");
        }
        final var budgets = projectStoragePort.findBudgets(command.getProjectId()).getBudgets();
        if (budgets.stream().noneMatch(budget -> budget.getCurrency() == command.getCurrency() &&
                                                 budget.getRemaining().compareTo(command.getAmount()) >= 0)) {
            throw OnlyDustException.badRequest(("Not enough budget of currency %s for project %s to request a " +
                                                "reward with an amount of %s")
                    .formatted(command.getCurrency(), command.getProjectId(), command.getAmount()));
        }

        indexerPort.indexUser(command.getRecipientId());
        return rewardServicePort.requestPayment(projectLeadId, command);
    }

    @Override
    public void cancelPayment(UUID projectLeadId, UUID projectId, UUID rewardId) {
        if (permissionService.isUserProjectLead(projectId, projectLeadId)) {
            rewardServicePort.cancelPayment(rewardId);
        } else {
            throw OnlyDustException.forbidden("User must be project lead to cancel a reward");
        }
    }

    @Override
    public void markInvoiceAsReceived(Long recipientId) {
        final var rewardIds = userStoragePort.findPendingInvoiceRewardsForRecipientId(recipientId).stream()
                .map(UserRewardView::getId).toList();
        rewardServicePort.markInvoiceAsReceived(rewardIds);
    }
}
