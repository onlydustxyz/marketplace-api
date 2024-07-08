package onlydust.com.marketplace.project.domain.service;

import com.github.javafaker.Faker;
import onlydust.com.marketplace.kernel.model.github.GithubUserIdentity;
import onlydust.com.marketplace.project.domain.model.CreateAndCloseIssueCommand;
import onlydust.com.marketplace.project.domain.model.RequestRewardCommand;
import onlydust.com.marketplace.project.domain.port.input.ProjectFacadePort;
import onlydust.com.marketplace.project.domain.port.input.RewardFacadePort;
import onlydust.com.marketplace.project.domain.port.output.GithubSearchPort;
import onlydust.com.marketplace.project.domain.port.output.ProjectCurrencyStoragePort;
import onlydust.com.marketplace.project.domain.port.output.ProjectStoragePort;
import onlydust.com.marketplace.project.domain.view.ProjectOrganizationRepoView;
import onlydust.com.marketplace.project.domain.view.ProjectOrganizationView;
import onlydust.com.marketplace.project.domain.view.RewardableItemView;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class AutomatedRewardServiceTest {

    private final Faker faker = new Faker();

    @Test
    void should_create_other_work_and_reward() {
        // Given
        final GithubSearchPort githubSearchPort = mock(GithubSearchPort.class);
        final ProjectFacadePort projectFacadePort = mock(ProjectFacadePort.class);
        final RewardFacadePort rewardFacadePort = mock(RewardFacadePort.class);
        final ProjectStoragePort projectStoragePort = mock(ProjectStoragePort.class);
        final ProjectCurrencyStoragePort projectCurrencyStoragePort = mock(ProjectCurrencyStoragePort.class);
        final AutomatedRewardService automatedRewardService = new AutomatedRewardService(githubSearchPort, projectFacadePort, rewardFacadePort,
                projectStoragePort, projectCurrencyStoragePort);
        final String projectSlug = faker.rickAndMorty().character();
        final UUID projectLeadId = UUID.randomUUID();
        final String repositoryName = faker.lordOfTheRings().character();
        final String reason = faker.lorem().sentence();
        final String recipientLogin = faker.pokemon().name();
        final String currencyCode = faker.animal().name();
        final BigDecimal amount = BigDecimal.valueOf(faker.number().randomNumber());
        final UUID projectId = UUID.randomUUID();
        final UUID currencyId = UUID.randomUUID();
        final long recipientId = 3L;
        final long otherWorkId = 5L;
        final String otherWorkTitle = "%s - Recipient github login : %s".formatted(reason, recipientLogin);
        final String otherWorkDescription = "Reward sent from OnlyDust admin";
        final long repoId = 2L;

        // When
        when(projectStoragePort.getProjectIdBySlug(projectSlug)).thenReturn(Optional.of(projectId));
        when(projectStoragePort.getProjectOrganizations(projectId))
                .thenReturn(List.of(ProjectOrganizationView.builder()
                        .id(1L)
                        .login(faker.rickAndMorty().location())
                        .isInstalled(true)
                        .repos(Set.of(ProjectOrganizationRepoView.builder()
                                .githubRepoId(repoId)
                                .name(repositoryName)
                                .build()))
                        .build()));
        when(projectCurrencyStoragePort.findCurrencyIdByCode(currencyCode)).thenReturn(Optional.of(currencyId));
        when(githubSearchPort.searchUsersByLogin(recipientLogin))
                .thenReturn(List.of(GithubUserIdentity.builder()
                        .githubUserId(recipientId)
                        .githubLogin(recipientLogin)
                        .build()));
        when(projectFacadePort.createAndCloseIssueForProjectIdAndRepositoryId(CreateAndCloseIssueCommand.builder()
                .projectId(projectId)
                .projectLeadId(projectLeadId)
                .githubRepoId(2L)
                .title(otherWorkTitle)
                .description(otherWorkDescription)
                .build())).thenReturn(RewardableItemView.builder()
                .number(otherWorkId)
                .id("5")
                .build());
        automatedRewardService.createOtherWorkAndReward(
                projectSlug,
                projectLeadId,
                repositoryName,
                reason,
                recipientLogin,
                currencyCode,
                amount
        );

        // Then
        final ArgumentCaptor<RequestRewardCommand> requestRewardCommandArgumentCaptor = ArgumentCaptor.forClass(RequestRewardCommand.class);
        final ArgumentCaptor<UUID> uuidArgumentCaptor = ArgumentCaptor.forClass(UUID.class);
        verify(rewardFacadePort).createReward(uuidArgumentCaptor.capture(), requestRewardCommandArgumentCaptor.capture());
        assertEquals(projectLeadId, uuidArgumentCaptor.getValue());
        assertEquals(projectId, requestRewardCommandArgumentCaptor.getValue().getProjectId());
        assertEquals(amount, requestRewardCommandArgumentCaptor.getValue().getAmount());
        assertEquals(currencyId, requestRewardCommandArgumentCaptor.getValue().getCurrencyId().value());
        assertEquals(recipientId, requestRewardCommandArgumentCaptor.getValue().getRecipientId());
        assertEquals(1, requestRewardCommandArgumentCaptor.getValue().getItems().size());
        assertEquals(RequestRewardCommand.Item.Type.issue, requestRewardCommandArgumentCaptor.getValue().getItems().get(0).getType());
        assertEquals(Long.toString(otherWorkId), requestRewardCommandArgumentCaptor.getValue().getItems().get(0).getId());
        assertEquals(repoId, requestRewardCommandArgumentCaptor.getValue().getItems().get(0).getRepoId());
        assertEquals(otherWorkId, requestRewardCommandArgumentCaptor.getValue().getItems().get(0).getNumber());
    }
}
