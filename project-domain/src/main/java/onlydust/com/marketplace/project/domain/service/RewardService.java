package onlydust.com.marketplace.project.domain.service;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.kernel.model.ProjectId;
import onlydust.com.marketplace.kernel.model.RewardId;
import onlydust.com.marketplace.kernel.model.UserId;
import onlydust.com.marketplace.kernel.port.output.IndexerPort;
import onlydust.com.marketplace.project.domain.gateway.CurrentDateProvider;
import onlydust.com.marketplace.project.domain.model.RequestRewardCommand;
import onlydust.com.marketplace.project.domain.model.Reward;
import onlydust.com.marketplace.project.domain.port.input.RewardFacadePort;
import onlydust.com.marketplace.project.domain.port.output.AccountingServicePort;
import onlydust.com.marketplace.project.domain.port.output.ContributionStoragePort;
import onlydust.com.marketplace.project.domain.port.output.RewardStoragePort;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static onlydust.com.marketplace.kernel.exception.OnlyDustException.forbidden;
import static onlydust.com.marketplace.kernel.exception.OnlyDustException.notFound;

@AllArgsConstructor
public class RewardService implements RewardFacadePort {
    private final RewardStoragePort rewardStoragePort;
    private final ContributionStoragePort contributionStoragePort;
    private final PermissionService permissionService;
    private final IndexerPort indexerPort;
    private final AccountingServicePort accountingServicePort;

    @Override
    @Transactional
    public RewardId createReward(UserId projectLeadId,
                                 RequestRewardCommand command) {
        if (!permissionService.isUserProjectLead(command.getProjectId(), projectLeadId))
            throw forbidden("User must be project lead to request a reward");

        if (command.getAmount().compareTo(BigDecimal.ZERO) <= 0)
            throw forbidden("Amount must be greater than 0");

        indexerPort.indexUser(command.getRecipientId());

        final var rewardId = RewardId.random();
        final var reward = new Reward(
                rewardId,
                command.getProjectId(),
                projectLeadId,
                command.getRecipientId(),
                command.getAmount(),
                command.getCurrencyId(),
                CurrentDateProvider.now(),
                command.getItems().stream().map(item -> Reward.Item.builder()
                        .contributionUUID(contributionStoragePort.getContributionUUID(item.getId())
                                .orElseThrow(() -> notFound("Contribution (repo %d, number %d, type %s) not found".formatted(
                                        item.getRepoId(), item.getNumber(), item.getType()))))
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

        accountingServicePort.createReward(command.getProjectId(), rewardId, command.getAmount(), command.getCurrencyId());
        return rewardId;
    }

    @Override
    @Transactional
    public void createRewards(UserId projectLeadId, List<RequestRewardCommand> rewardRequestCommands) {
        for (RequestRewardCommand rewardRequestCommand : rewardRequestCommands) {
            createReward(projectLeadId, rewardRequestCommand);
        }
    }

    @Override
    @Transactional
    public void cancelReward(UserId projectLeadId, ProjectId projectId, RewardId rewardId) {
        if (!permissionService.isUserProjectLead(projectId, projectLeadId))
            throw forbidden("User must be project lead to cancel a reward");

        final var reward = rewardStoragePort.get(rewardId)
                .orElseThrow(() -> notFound("Reward %s not found".formatted(rewardId)));

        accountingServicePort.cancelReward(rewardId, reward.currencyId());
        rewardStoragePort.delete(rewardId);
    }
}
