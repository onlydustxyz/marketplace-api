package onlydust.com.marketplace.project.domain.service;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import onlydust.com.marketplace.project.domain.model.RequestRewardCommand;
import onlydust.com.marketplace.project.domain.model.Reward;
import onlydust.com.marketplace.project.domain.port.input.RewardFacadePort;
import onlydust.com.marketplace.project.domain.port.output.IndexerPort;
import onlydust.com.marketplace.project.domain.port.output.ProjectRewardStoragePort;
import onlydust.com.marketplace.project.domain.port.output.RewardServicePort;
import onlydust.com.marketplace.project.domain.port.output.UserStoragePort;
import onlydust.com.marketplace.project.domain.view.UserRewardView;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@AllArgsConstructor
public class RewardService implements RewardFacadePort {

    private final RewardServicePort rewardServicePort;
    private final ProjectRewardStoragePort projectRewardStoragePort;
    private final PermissionService permissionService;
    private final IndexerPort indexerPort;
    private final UserStoragePort userStoragePort;

    @Override
    public UUID createReward(UUID projectLeadId,
                             RequestRewardCommand command) {
        if (!permissionService.isUserProjectLead(command.getProjectId(), projectLeadId)) {
            throw OnlyDustException.forbidden("User must be project lead to request a reward");
        }
        if (command.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw OnlyDustException.forbidden("Amount must be greater than 0");
        }
        final var budgets = projectRewardStoragePort.findBudgets(command.getProjectId()).getBudgets();
        if (budgets.stream().noneMatch(budget -> budget.getCurrency() == command.getCurrency() &&
                                                 budget.getRemaining().compareTo(command.getAmount()) >= 0)) {
            throw OnlyDustException.badRequest(("Not enough budget of currency %s for project %s to request a " +
                                                "reward with an amount of %s")
                    .formatted(command.getCurrency(), command.getProjectId(), command.getAmount()));
        }

        indexerPort.indexUser(command.getRecipientId());
        final var rewardId = rewardServicePort.create(projectLeadId, command);
        projectRewardStoragePort.updateUsdAmount(rewardId);
        return rewardId;
    }

    @Override
    public void cancelReward(UUID projectLeadId, UUID projectId, UUID rewardId) {
        if (permissionService.isUserProjectLead(projectId, projectLeadId)) {
            rewardServicePort.cancel(rewardId);
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

    @Override
    public Optional<Reward> getReward(UUID rewardId) {
        throw new UnsupportedOperationException("Not implemented in v1");
    }
}
