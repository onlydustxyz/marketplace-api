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
    @Transactional
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


            final Optional<Integer> optionalContributorLevel = nodeGuardiansApiPort.getContributorLevel(recipientLogin);
            if (optionalContributorLevel.isPresent()) {
                final Integer contributorLevel = optionalContributorLevel.get();
                if (contributorLevel > 0 && contributorLevel <= 3) {

                    final BigDecimal boostRate = switch (contributorLevel) {
                        case 1:
                            yield BigDecimal.valueOf(0.02D);
                        case 2:
                            yield BigDecimal.valueOf(0.05D);
                        case 3:
                            yield BigDecimal.valueOf(0.10D);
                        default:
                            throw OnlyDustException.internalServerError("Not supported NodeGuardians level %s for contributor %s".formatted(contributorLevel,
                                    recipientLogin));
                    };

                    for (Map.Entry<CurrencyView.Id, List<ShortProjectRewardView>> currencyRewardsEntry : recipientRewardsMapToCurrency.entrySet()) {

                        final List<ShortProjectRewardView> rewardsToBoostOnSingleCurrency = currencyRewardsEntry.getValue();

                        final BigDecimal rewardedAmountToBoost = rewardsToBoostOnSingleCurrency.stream()
                                .map(ShortProjectRewardView::getMoney)
                                .map(Money::amount)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                        // Unique constraint on rewardId x recipientId in DB should prevent duplicate boost resulting in an abnormal behavior
                        boostedRewardStoragePort.markRewardsAsBoosted(rewardsToBoostOnSingleCurrency.stream().map(ShortProjectRewardView::getRewardId).toList(),
                                recipientGithubUserId);


                        nodeGuardiansRewardBoostoutboxPort.push(BoostNodeGuardiansRewards.builder()
                                .amount(rewardedAmountToBoost.multiply(boostRate))
                                .projectId(projectId)
                                .currencyId(currencyRewardsEntry.getKey())
                                .recipientId(recipientGithubUserId)
                                .recipientLogin(recipientLogin)
                                .boostedRewards(rewardsToBoostOnSingleCurrency.stream().map(
                                        shortProjectRewardView -> BoostNodeGuardiansRewards.BoostedReward.builder()
                                                .currencyCode(shortProjectRewardView.getMoney().currency().code())
                                                .projectName(shortProjectRewardView.getProjectName())
                                                .amount(shortProjectRewardView.getMoney().amount())
                                                .id(shortProjectRewardView.getRewardId())
                                                .build()
                                ).toList())
                                .repoId(githubRepoId)
                                .projectLeadId(projectLeadId)
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

            final RewardableItemView otherWork = projectFacadePort.createAndCloseIssueForProjectIdAndRepositoryId(
                    CreateAndCloseIssueCommand.builder()
                            .projectId(boostNodeGuardiansRewards.getProjectId())
                            .projectLeadId(boostNodeGuardiansRewards.getProjectLeadId())
                            .githubRepoId(boostNodeGuardiansRewards.getRepoId())
                            .title("Node Guardians boost #%s for contributor %s"
                                    .formatted(boostedRewardStoragePort.getBoostedRewardsCountByRecipientId(boostNodeGuardiansRewards.getRecipientId()).orElse(0) + 1,
                                            boostNodeGuardiansRewards.getRecipientLogin()))
                            .description(String.join("\n", boostNodeGuardiansRewards.getBoostedRewards().stream()
                                    .map(r -> String.join(" - ", "#" + r.getId().toString().substring(0, 5).toUpperCase(), r.getProjectName(),
                                            r.getCurrencyCode(), r.getAmount().toString()))
                                    .toList()))
                            .build()
            );

            final RequestRewardCommand requestRewardCommand = RequestRewardCommand.builder()
                    .amount(boostNodeGuardiansRewards.getAmount())
                    .projectId(boostNodeGuardiansRewards.getProjectId())
                    .currencyId(boostNodeGuardiansRewards.getCurrencyId())
                    .recipientId(boostNodeGuardiansRewards.getRecipientId())
                    .items(List.of(RequestRewardCommand.Item.builder()
                            .id(otherWork.getId())
                            .type(RequestRewardCommand.Item.Type.issue)
                            .number(otherWork.getNumber())
                            .repoId(boostNodeGuardiansRewards.getRepoId())
                            .build()))
                    .build();
            final UUID rewardId = rewardFacadePort.createReward(boostNodeGuardiansRewards.getProjectLeadId(), requestRewardCommand);
            boostedRewardStoragePort.updateBoostedRewardsWithBoostRewardId(
                    boostNodeGuardiansRewards.getBoostedRewards().stream().map(BoostNodeGuardiansRewards.BoostedReward::getId).toList(),
                    boostNodeGuardiansRewards.getRecipientId(), rewardId);
        } else {
            LOGGER.error("Invalid event class %s for NodeGuardians reward boost outbox consumer".formatted(event.getClass()));
        }
    }
}
