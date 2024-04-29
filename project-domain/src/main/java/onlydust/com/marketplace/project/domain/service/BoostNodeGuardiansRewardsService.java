package onlydust.com.marketplace.project.domain.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import onlydust.com.marketplace.kernel.model.CurrencyView;
import onlydust.com.marketplace.kernel.model.Event;
import onlydust.com.marketplace.kernel.port.output.OutboxConsumer;
import onlydust.com.marketplace.kernel.port.output.OutboxPort;
import onlydust.com.marketplace.project.domain.model.CreateAndCloseIssueCommand;
import onlydust.com.marketplace.project.domain.model.RequestRewardCommand;
import onlydust.com.marketplace.project.domain.model.event.BoostNodeGuardiansRewards;
import onlydust.com.marketplace.project.domain.port.input.BoostNodeGuardiansRewardsPort;
import onlydust.com.marketplace.project.domain.port.input.ProjectFacadePort;
import onlydust.com.marketplace.project.domain.port.input.RewardFacadePort;
import onlydust.com.marketplace.project.domain.port.output.BoostedRewardStoragePort;
import onlydust.com.marketplace.project.domain.port.output.NodeGuardiansApiPort;
import onlydust.com.marketplace.project.domain.view.ContributorLinkView;
import onlydust.com.marketplace.project.domain.view.Money;
import onlydust.com.marketplace.project.domain.view.RewardableItemView;
import onlydust.com.marketplace.project.domain.view.ShortProjectRewardView;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@AllArgsConstructor
public class BoostNodeGuardiansRewardsService implements BoostNodeGuardiansRewardsPort, OutboxConsumer {

    private final ProjectFacadePort projectFacadePort;
    private final BoostedRewardStoragePort boostedRewardStoragePort;
    private final RewardFacadePort rewardFacadePort;
    private final NodeGuardiansApiPort nodeGuardiansApiPort;
    private final OutboxPort nodeGuardiansRewardBoostoutboxPort;

    @Override
    public void boostProject(UUID projectId, UUID projectLeadId, Long githubRepoId, UUID ecosystemId) {
        List<ShortProjectRewardView> rewardsToBoost = boostedRewardStoragePort.getRewardsToBoostFromEcosystemNotLinkedToProject(ecosystemId, projectId);

        final Map<ContributorLinkView, List<ShortProjectRewardView>> rewardsMapToRecipientLogin = rewardsToBoost.stream()
                .collect(Collectors.groupingBy(ShortProjectRewardView::getRecipient));

        for (Map.Entry<ContributorLinkView, List<ShortProjectRewardView>> recipientRewardsEntry : rewardsMapToRecipientLogin.entrySet()) {

            final List<ShortProjectRewardView> rewardsToBoostForRecipient = recipientRewardsEntry.getValue();
            final String recipientLogin = recipientRewardsEntry.getKey().getLogin();
            final Long recipientGithubUserId = recipientRewardsEntry.getKey().getGithubUserId();

            final Map<CurrencyView.Id, List<ShortProjectRewardView>> recipientRewardsMapToCurrency = rewardsToBoostForRecipient.stream()
                    .collect(Collectors.groupingBy(shortProjectRewardView -> shortProjectRewardView.getMoney().currency().id()));

            for (Map.Entry<CurrencyView.Id, List<ShortProjectRewardView>> currencyRewardsEntry : recipientRewardsMapToCurrency.entrySet()) {

                final List<ShortProjectRewardView> rewardsToBoostOnSingleCurrency = currencyRewardsEntry.getValue();

                final Optional<Integer> optionalContributorLevel = nodeGuardiansApiPort.getContributorLevel(recipientLogin);
                if (optionalContributorLevel.isPresent()) {
                    final Integer contributorLevel = optionalContributorLevel.get();
                    if (contributorLevel > 0 && contributorLevel <= 3) {
                        final BigDecimal rewardedAmountToBoost = rewardsToBoostOnSingleCurrency.stream()
                                .map(ShortProjectRewardView::getMoney)
                                .map(Money::amount)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                        double boostRate = switch (contributorLevel) {
                            case 1:
                                yield 0.02D;
                            case 2:
                                yield 0.05D;
                            case 3:
                                yield 0.10D;
                            default:
                                throw OnlyDustException.internalServerError("Not supported NodeGuardians level %s for contributor %s".formatted(contributorLevel,
                                        recipientLogin));
                        };

                        // Unique constraint on rewardId x recipientId in DB should prevent duplicate boost resulting in an abnormal behavior
                        boostedRewardStoragePort.markRewardsAsBoosted(rewardsToBoostOnSingleCurrency.stream().map(ShortProjectRewardView::getRewardId).toList(),
                                recipientGithubUserId);

                        final RewardableItemView otherWork = projectFacadePort.createAndCloseIssueForProjectIdAndRepositoryId(
                                CreateAndCloseIssueCommand.builder()
                                        .projectId(projectId)
                                        .projectLeadId(projectLeadId)
                                        .githubRepoId(githubRepoId)
                                        .title("Node Guardians boost #%s for contributor %s"
                                                .formatted(boostedRewardStoragePort.getBoostedRewardsCountByRecipientId(recipientGithubUserId).orElse(0) + 1,
                                                        recipientLogin))
                                        .description(String.join("\n", rewardsToBoostOnSingleCurrency.stream()
                                                .map(r -> String.join(" - ", "#" + r.getRewardId().toString().substring(0, 5).toUpperCase(), r.getProjectName(),
                                                        r.getMoney().currency().code(), r.getMoney().amount().toString()))
                                                .toList()))
                                        .build()
                        );
                        nodeGuardiansRewardBoostoutboxPort.push(BoostNodeGuardiansRewards.builder()
                                .amount(rewardedAmountToBoost.multiply(BigDecimal.valueOf(boostRate)))
                                .projectId(projectId)
                                .currencyId(currencyRewardsEntry.getKey())
                                .recipientId(recipientGithubUserId)
                                .issueId(otherWork.getId())
                                .issueNumber(otherWork.getNumber())
                                .repoId(githubRepoId)
                                .projectLeadId(projectLeadId)
                                .boostedRewardIds(rewardsToBoostOnSingleCurrency.stream().map(ShortProjectRewardView::getRewardId).toList())
                                .build());
                    }
                } else {
                    LOGGER.info("Contributor named %s has not contributed on NodeGuardians, skipping %s reward(s) to boost".formatted(recipientLogin,
                            rewardsToBoostForRecipient.size()));
                }
            }
        }
    }

    @Override
    @Transactional
    public void process(Event event) {
        if (event instanceof BoostNodeGuardiansRewards boostNodeGuardiansRewards) {
            final RequestRewardCommand requestRewardCommand = RequestRewardCommand.builder()
                    .amount(boostNodeGuardiansRewards.getAmount())
                    .projectId(boostNodeGuardiansRewards.getProjectId())
                    .currencyId(boostNodeGuardiansRewards.getCurrencyId())
                    .recipientId(boostNodeGuardiansRewards.getRecipientId())
                    .items(List.of(RequestRewardCommand.Item.builder()
                            .id(boostNodeGuardiansRewards.getIssueId())
                            .type(RequestRewardCommand.Item.Type.issue)
                            .number(boostNodeGuardiansRewards.getIssueNumber())
                            .repoId(boostNodeGuardiansRewards.getRepoId())
                            .build()))
                    .build();
            final UUID rewardId = rewardFacadePort.createReward(boostNodeGuardiansRewards.getProjectLeadId(), requestRewardCommand);
            boostedRewardStoragePort.updateBoostedRewardsWithBoostRewardId(boostNodeGuardiansRewards.getBoostedRewardIds(),
                    boostNodeGuardiansRewards.getRecipientId(), rewardId);
        } else {
            LOGGER.error("Invalid event class %s for NodeGuardians reward boost outbox consumer".formatted(event.getClass()));
        }
    }
}
