package onlydust.com.marketplace.project.domain.service;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import onlydust.com.marketplace.kernel.port.output.IndexerPort;
import onlydust.com.marketplace.project.domain.model.OldRequestRewardCommand;
import onlydust.com.marketplace.project.domain.model.Reward;
import onlydust.com.marketplace.project.domain.port.input.RewardFacadePort;
import onlydust.com.marketplace.project.domain.port.output.AccountingServicePort;
import onlydust.com.marketplace.project.domain.port.output.RewardStoragePort;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@AllArgsConstructor
public class RewardService implements RewardFacadePort {
    private final RewardStoragePort rewardStoragePort;
    private final PermissionService permissionService;
    private final IndexerPort indexerPort;
    private final AccountingServicePort accountingServicePort;

    @Override
    @Transactional
    public UUID createReward(UUID projectLeadId,
                             OldRequestRewardCommand command) {
        if (!permissionService.isUserProjectLead(command.getProjectId(), projectLeadId))
            throw OnlyDustException.forbidden("User must be project lead to request a reward");

        if (command.getAmount().compareTo(BigDecimal.ZERO) <= 0)
            throw OnlyDustException.forbidden("Amount must be greater than 0");

        indexerPort.indexUser(command.getRecipientId());

        final var rewardId = UUID.randomUUID();
        // TODO: Refactor command to match new types
        final var reward = new Reward(
                rewardId,
                command.getProjectId(),
                projectLeadId,
                command.getRecipientId(),
                command.getAmount(),
                command.getCurrency(),
                new Date(),
                command.getItems().stream().map(item -> Reward.Item.builder()
                        .id(item.getId())
                        .number(item.getNumber())
                        .repoId(item.getRepoId())
                        .type(switch (item.getType()) {
                            case issue -> Reward.Item.Type.ISSUE;
                            case pullRequest -> Reward.Item.Type.PULL_REQUEST;
                            case codeReview -> Reward.Item.Type.CODE_REVIEW;
                        })
                        .build()).toList()
        );
        rewardStoragePort.save(reward);

        // TODO: Use currencyId as input in REST API
        accountingServicePort.createReward(command.getProjectId(), rewardId, command.getAmount(), command.getCurrency().toString().toUpperCase());
        return rewardId;
    }

    @Override
    @Transactional
    public void cancelReward(UUID projectLeadId, UUID projectId, UUID rewardId) {
        if (!permissionService.isUserProjectLead(projectId, projectLeadId))
            throw OnlyDustException.forbidden("User must be project lead to cancel a reward");

        final var reward = rewardStoragePort.get(rewardId)
                .orElseThrow(() -> OnlyDustException.notFound("Reward %s not found".formatted(rewardId)));
        // TODO: prevent cancel if reward already in invoice

        // TODO: Use currencyId as input in REST API
        rewardStoragePort.delete(rewardId);
        accountingServicePort.cancelReward(rewardId, reward.currency().toString().toUpperCase());
    }

    @Override
    public Optional<Reward> getReward(UUID rewardId) {
        return rewardStoragePort.get(rewardId);
    }
}
