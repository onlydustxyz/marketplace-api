package onlydust.com.marketplace.project.domain.service;

import com.github.javafaker.Faker;
import onlydust.com.marketplace.kernel.model.CurrencyView;
import onlydust.com.marketplace.kernel.port.output.OutboxPort;
import onlydust.com.marketplace.project.domain.model.CreateAndCloseIssueCommand;
import onlydust.com.marketplace.project.domain.model.RequestRewardCommand;
import onlydust.com.marketplace.project.domain.model.event.BoostNodeGuardiansRewards;
import onlydust.com.marketplace.project.domain.port.input.ProjectFacadePort;
import onlydust.com.marketplace.project.domain.port.input.RewardFacadePort;
import onlydust.com.marketplace.project.domain.port.output.BoostedRewardStoragePort;
import onlydust.com.marketplace.project.domain.port.output.NodeGuardiansApiPort;
import onlydust.com.marketplace.project.domain.view.ContributorLinkView;
import onlydust.com.marketplace.project.domain.view.Money;
import onlydust.com.marketplace.project.domain.view.RewardableItemView;
import onlydust.com.marketplace.project.domain.view.ShortProjectRewardView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static org.mockito.Mockito.*;

public class BoostNodeGuardiansRewardsServiceTest {

    BoostNodeGuardiansRewardsService boostNodeGuardiansRewardsService;
    private ProjectFacadePort projectFacadePort;
    private BoostedRewardStoragePort boostedRewardStoragePort;
    private RewardFacadePort rewardFacadePort;
    private NodeGuardiansApiPort nodeGuardiansApiPort;
    private OutboxPort nodeGuardiansRewardBoostoutboxPort;
    private final Faker faker = new Faker();

    @BeforeEach
    void setUp() {
        projectFacadePort = mock(ProjectFacadePort.class);
        boostedRewardStoragePort = mock(BoostedRewardStoragePort.class);
        rewardFacadePort = mock(RewardFacadePort.class);
        nodeGuardiansApiPort = mock(NodeGuardiansApiPort.class);
        nodeGuardiansRewardBoostoutboxPort = mock(OutboxPort.class);
        boostNodeGuardiansRewardsService = new BoostNodeGuardiansRewardsService(projectFacadePort, boostedRewardStoragePort, rewardFacadePort,
                nodeGuardiansApiPort, nodeGuardiansRewardBoostoutboxPort);
    }

    @Test
    void should_boost_rewards() {
        // Given
        final long githubRepoId = faker.number().randomNumber();
        final UUID projectLeadId = UUID.randomUUID();
        final UUID projectId = UUID.randomUUID();
        final UUID ecosystemId = UUID.randomUUID();
        final ContributorLinkView recipient1 = ContributorLinkView.builder()
                .login(faker.gameOfThrones().character())
                .githubUserId(faker.number().randomNumber())
                .build();
        final ContributorLinkView recipient2 = ContributorLinkView.builder()
                .login(faker.lordOfTheRings().character())
                .githubUserId(recipient1.getGithubUserId() + faker.number().randomNumber())
                .build();
        final CurrencyView strk = CurrencyView.builder()
                .decimals(2)
                .id(CurrencyView.Id.random())
                .name("Starknet token")
                .code("STRK")
                .build();
        final CurrencyView usd = CurrencyView.builder()
                .decimals(3)
                .id(CurrencyView.Id.random())
                .name("Dollars")
                .code("USD")
                .build();

        final ShortProjectRewardView r11 = ShortProjectRewardView.builder()
                .recipient(recipient1)
                .money(new Money(BigDecimal.valueOf(120), strk))
                .rewardId(UUID.randomUUID())
                .projectName(faker.rickAndMorty().character())
                .build();
        final ShortProjectRewardView r12 = ShortProjectRewardView.builder()
                .recipient(recipient1)
                .money(new Money(BigDecimal.valueOf(230), strk))
                .rewardId(UUID.randomUUID())
                .projectName(faker.rickAndMorty().character())
                .build();
        final ShortProjectRewardView r13 = ShortProjectRewardView.builder()
                .recipient(recipient1)
                .money(new Money(BigDecimal.valueOf(1000), usd))
                .rewardId(UUID.randomUUID())
                .projectName(faker.rickAndMorty().character())
                .build();
        final ShortProjectRewardView r21 = ShortProjectRewardView.builder()
                .recipient(recipient2)
                .money(new Money(BigDecimal.valueOf(2000), usd))
                .rewardId(UUID.randomUUID())
                .projectName(faker.rickAndMorty().character())
                .build();
        final ShortProjectRewardView r22 = ShortProjectRewardView.builder()
                .recipient(recipient2)
                .money(new Money(BigDecimal.valueOf(250.4), strk))
                .rewardId(UUID.randomUUID())
                .projectName(faker.rickAndMorty().character())
                .build();
        final List<ShortProjectRewardView> shortProjectRewardViews = List.of(
                r11,
                r12,
                r13,
                r21,
                r22
        );
        final RewardableItemView otherWork11 = RewardableItemView.builder()
                .id("11")
                .number(11L)
                .build();
        final RewardableItemView otherWork12 = RewardableItemView.builder()
                .id("12")
                .number(12L)
                .build();
        final RewardableItemView otherWork21 = RewardableItemView.builder()
                .id("21")
                .number(21L)
                .build();
        final RewardableItemView otherWork22 = RewardableItemView.builder()
                .id("22")
                .number(22L)
                .build();
        final UUID rBoosted11 = UUID.randomUUID();
        final UUID rBoosted12 = UUID.randomUUID();
        final UUID rBoosted21 = UUID.randomUUID();
        final UUID rBoosted22 = UUID.randomUUID();
        final BoostNodeGuardiansRewards e11 = BoostNodeGuardiansRewards.builder()
                .amount(Stream.of(r11, r12).map(ShortProjectRewardView::getMoney).map(Money::amount).reduce(BigDecimal::add).get().multiply(BigDecimal.valueOf(0.02D)))
                .projectId(projectId)
                .currencyId(strk.id())
                .recipientId(recipient1.getGithubUserId())
                .recipientLogin(recipient1.getLogin())
                .repoId(githubRepoId)
                .projectLeadId(projectLeadId)
                .boostedRewards(List.of(fromView(r11), fromView(r12)))
                .build();
        final BoostNodeGuardiansRewards e12 = BoostNodeGuardiansRewards.builder()
                .amount(Stream.of(r13).map(ShortProjectRewardView::getMoney).map(Money::amount).reduce(BigDecimal::add).get().multiply(BigDecimal.valueOf(0.02D)))
                .projectId(projectId)
                .currencyId(usd.id())
                .recipientId(recipient1.getGithubUserId())
                .recipientLogin(recipient1.getLogin())
                .repoId(githubRepoId)
                .projectLeadId(projectLeadId)
                .boostedRewards(List.of(fromView(r13)))
                .build();
        final BoostNodeGuardiansRewards e21 = BoostNodeGuardiansRewards.builder()
                .amount(Stream.of(r21).map(ShortProjectRewardView::getMoney).map(Money::amount).reduce(BigDecimal::add).get().multiply(BigDecimal.valueOf(0.05D)))
                .projectId(projectId)
                .currencyId(usd.id())
                .recipientId(recipient2.getGithubUserId())
                .recipientLogin(recipient2.getLogin())
                .repoId(githubRepoId)
                .projectLeadId(projectLeadId)
                .boostedRewards(List.of(fromView(r21)))
                .build();
        final BoostNodeGuardiansRewards e22 = BoostNodeGuardiansRewards.builder()
                .amount(Stream.of(r22).map(ShortProjectRewardView::getMoney).map(Money::amount).reduce(BigDecimal::add).get().multiply(BigDecimal.valueOf(0.05D)))
                .projectId(projectId)
                .currencyId(strk.id())
                .recipientId(recipient2.getGithubUserId())
                .recipientLogin(recipient2.getLogin())
                .repoId(githubRepoId)
                .projectLeadId(projectLeadId)
                .boostedRewards(List.of(fromView(r22)))
                .build();


        // When
        when(boostedRewardStoragePort.getRewardsToBoostFromEcosystemNotLinkedToProject(ecosystemId, projectId))
                .thenReturn(shortProjectRewardViews);
        when(boostedRewardStoragePort.getBoostedRewardsCountByRecipientId(recipient1.getGithubUserId())).thenReturn(Optional.of(1));
        when(boostedRewardStoragePort.getBoostedRewardsCountByRecipientId(recipient2.getGithubUserId())).thenReturn(Optional.of(2));
        when(nodeGuardiansApiPort.getContributorLevel(recipient1.getLogin())).thenReturn(Optional.of(1));
        when(nodeGuardiansApiPort.getContributorLevel(recipient2.getLogin())).thenReturn(Optional.of(2));
        boostNodeGuardiansRewardsService.boostProject(projectId, projectLeadId, githubRepoId, ecosystemId);

        // Then
        verify(boostedRewardStoragePort).markRewardsAsBoosted(List.of(r11.getRewardId(), r12.getRewardId()), recipient1.getGithubUserId());
        verify(boostedRewardStoragePort).markRewardsAsBoosted(List.of(r13.getRewardId()), recipient1.getGithubUserId());
        verify(boostedRewardStoragePort).markRewardsAsBoosted(List.of(r21.getRewardId()), recipient2.getGithubUserId());
        verify(boostedRewardStoragePort).markRewardsAsBoosted(List.of(r22.getRewardId()), recipient2.getGithubUserId());
        verify(nodeGuardiansRewardBoostoutboxPort).push(e11);
        verify(nodeGuardiansRewardBoostoutboxPort).push(e12);
        verify(nodeGuardiansRewardBoostoutboxPort).push(e21);
        verify(nodeGuardiansRewardBoostoutboxPort).push(e22);


        // when
        when(projectFacadePort.createAndCloseIssueForProjectIdAndRepositoryId(createOtherWorkerFromStubs(projectId, projectLeadId, githubRepoId,
                List.of(r11, r12), 1))).thenReturn(otherWork11);
        when(projectFacadePort.createAndCloseIssueForProjectIdAndRepositoryId(createOtherWorkerFromStubs(projectId, projectLeadId, githubRepoId,
                List.of(r13), 1))).thenReturn(otherWork12);
        when(projectFacadePort.createAndCloseIssueForProjectIdAndRepositoryId(createOtherWorkerFromStubs(projectId, projectLeadId, githubRepoId,
                List.of(r21), 2))).thenReturn(otherWork21);
        when(projectFacadePort.createAndCloseIssueForProjectIdAndRepositoryId(createOtherWorkerFromStubs(projectId, projectLeadId, githubRepoId,
                List.of(r22), 2))).thenReturn(otherWork22);
        when(rewardFacadePort.createReward(projectLeadId, RequestRewardCommand.builder()
                .amount(Stream.of(r11, r12).map(ShortProjectRewardView::getMoney).map(Money::amount).reduce(BigDecimal::add).get().multiply(BigDecimal.valueOf(0.02D)))
                .projectId(projectId)
                .currencyId(strk.id())
                .recipientId(recipient1.getGithubUserId())
                .items(List.of(RequestRewardCommand.Item.builder()
                        .id(otherWork11.getId())
                        .type(RequestRewardCommand.Item.Type.issue)
                        .number(otherWork11.getNumber())
                        .repoId(githubRepoId)
                        .build()))
                .build())).thenReturn(rBoosted11);
        when(rewardFacadePort.createReward(projectLeadId, RequestRewardCommand.builder()
                .amount(Stream.of(r13).map(ShortProjectRewardView::getMoney).map(Money::amount).reduce(BigDecimal::add).get().multiply(BigDecimal.valueOf(0.02D)))
                .projectId(projectId)
                .currencyId(usd.id())
                .recipientId(recipient1.getGithubUserId())
                .items(List.of(RequestRewardCommand.Item.builder()
                        .id(otherWork12.getId())
                        .type(RequestRewardCommand.Item.Type.issue)
                        .number(otherWork12.getNumber())
                        .repoId(githubRepoId)
                        .build()))
                .build())).thenReturn(rBoosted12);
        when(rewardFacadePort.createReward(projectLeadId, RequestRewardCommand.builder()
                .amount(Stream.of(r21).map(ShortProjectRewardView::getMoney).map(Money::amount).reduce(BigDecimal::add).get().multiply(BigDecimal.valueOf(0.05D)))
                .projectId(projectId)
                .currencyId(usd.id())
                .recipientId(recipient2.getGithubUserId())
                .items(List.of(RequestRewardCommand.Item.builder()
                        .id(otherWork21.getId())
                        .type(RequestRewardCommand.Item.Type.issue)
                        .number(otherWork21.getNumber())
                        .repoId(githubRepoId)
                        .build()))
                .build())).thenReturn(rBoosted21);
        when(rewardFacadePort.createReward(projectLeadId, RequestRewardCommand.builder()
                .amount(Stream.of(r22).map(ShortProjectRewardView::getMoney).map(Money::amount).reduce(BigDecimal::add).get().multiply(BigDecimal.valueOf(0.05D)))
                .projectId(projectId)
                .currencyId(strk.id())
                .recipientId(recipient2.getGithubUserId())
                .items(List.of(RequestRewardCommand.Item.builder()
                        .id(otherWork22.getId())
                        .type(RequestRewardCommand.Item.Type.issue)
                        .number(otherWork22.getNumber())
                        .repoId(githubRepoId)
                        .build()))
                .build())).thenReturn(rBoosted22);
        boostNodeGuardiansRewardsService.process(e11);
        boostNodeGuardiansRewardsService.process(e12);
        boostNodeGuardiansRewardsService.process(e21);
        boostNodeGuardiansRewardsService.process(e22);

        // Then
        verify(boostedRewardStoragePort).updateBoostedRewardsWithBoostRewardId(List.of(r11.getRewardId(), r12.getRewardId()), recipient1.getGithubUserId(),
                rBoosted11);
        verify(boostedRewardStoragePort).updateBoostedRewardsWithBoostRewardId(List.of(r13.getRewardId()), recipient1.getGithubUserId(), rBoosted12);
        verify(boostedRewardStoragePort).updateBoostedRewardsWithBoostRewardId(List.of(r21.getRewardId()), recipient2.getGithubUserId(), rBoosted21);
        verify(boostedRewardStoragePort).updateBoostedRewardsWithBoostRewardId(List.of(r22.getRewardId()), recipient2.getGithubUserId(), rBoosted22);
    }

    private CreateAndCloseIssueCommand createOtherWorkerFromStubs(final UUID projectId, final UUID projectLeadId, final Long githubRepoId,
                                                                  final List<ShortProjectRewardView> shortProjectRewardViews,
                                                                  final Integer expectedBoostCount) {
        return CreateAndCloseIssueCommand.builder()
                .projectId(projectId)
                .projectLeadId(projectLeadId)
                .githubRepoId(githubRepoId)
                .title("Node Guardians boost #%s for contributor %s"
                        .formatted(expectedBoostCount, shortProjectRewardViews.get(0).getRecipient().getLogin()))
                .description(String.join("\n", shortProjectRewardViews.stream()
                        .map(r -> String.join(" - ", "#" + r.getRewardId().toString().substring(0, 5).toUpperCase(), r.getProjectName(),
                                r.getMoney().currency().code(), r.getMoney().amount().toString()))
                        .toList()))
                .build();
    }

    private BoostNodeGuardiansRewards.BoostedReward fromView(final ShortProjectRewardView shortProjectRewardView) {
        return BoostNodeGuardiansRewards.BoostedReward.builder()
                .currencyCode(shortProjectRewardView.getMoney().currency().code())
                .projectName(shortProjectRewardView.getProjectName())
                .amount(shortProjectRewardView.getMoney().amount())
                .id(shortProjectRewardView.getRewardId())
                .build();
    }
}
