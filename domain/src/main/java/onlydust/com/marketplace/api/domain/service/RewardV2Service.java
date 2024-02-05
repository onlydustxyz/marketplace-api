package onlydust.com.marketplace.api.domain.service;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.domain.model.RequestRewardCommand;
import onlydust.com.marketplace.api.domain.port.input.RewardFacadePort;
import onlydust.com.marketplace.api.domain.port.output.AccountingServicePort;
import onlydust.com.marketplace.api.domain.port.output.IndexerPort;
import onlydust.com.marketplace.api.domain.port.output.RewardServicePort;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.util.UUID;

@AllArgsConstructor
public class RewardV2Service implements RewardFacadePort {

    private final RewardServicePort rewardServicePort;
    private final PermissionService permissionService;
    private final IndexerPort indexerPort;
    private final AccountingServicePort accountingServicePort;

    @Override
    @Transactional
    public UUID requestPayment(UUID projectLeadId,
                               RequestRewardCommand command) {
        if (!permissionService.isUserProjectLead(command.getProjectId(), projectLeadId))
            throw OnlyDustException.forbidden("User must be project lead to request a reward");

        if (command.getAmount().compareTo(BigDecimal.ZERO) <= 0)
            throw OnlyDustException.forbidden("Amount must be greater than 0");

        indexerPort.indexUser(command.getRecipientId());

        final var rewardId = rewardServicePort.create(projectLeadId, command);
        // TODO: Use currencyId as input in REST API
        accountingServicePort.createReward(command.getProjectId(), rewardId, command.getAmount(), command.getCurrency().toString().toUpperCase());
        return rewardId;
    }

    @Override
    @Transactional
    public void cancelPayment(UUID projectLeadId, UUID projectId, UUID rewardId) {
        if (!permissionService.isUserProjectLead(projectId, projectLeadId))
            throw OnlyDustException.forbidden("User must be project lead to cancel a reward");

        final var reward = rewardServicePort.get(rewardId)
                .orElseThrow(() -> OnlyDustException.notFound("Reward %s not found".formatted(rewardId)));

        // TODO: Use currencyId as input in REST API
        rewardServicePort.cancel(rewardId);
        accountingServicePort.cancelReward(projectId, rewardId, reward.amount(), reward.currency().toString().toUpperCase());
    }

    @Override
    public void markInvoiceAsReceived(Long recipientId) {
        // TODO
        throw OnlyDustException.internalServerError("Not implemented yet");
    }
}
