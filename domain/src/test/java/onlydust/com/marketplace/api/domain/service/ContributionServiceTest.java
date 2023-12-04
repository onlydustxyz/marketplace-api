package onlydust.com.marketplace.api.domain.service;

import com.github.javafaker.Faker;
import onlydust.com.marketplace.api.domain.exception.OnlyDustException;
import onlydust.com.marketplace.api.domain.model.ContributionStatus;
import onlydust.com.marketplace.api.domain.model.User;
import onlydust.com.marketplace.api.domain.port.output.ContributionStoragePort;
import onlydust.com.marketplace.api.domain.view.ContributionDetailsView;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class ContributionServiceTest {

    final ContributionStoragePort contributionStoragePort = mock(ContributionStoragePort.class);
    final PermissionService permissionService = mock(PermissionService.class);

    final ContributionService contributionService = new ContributionService(contributionStoragePort, permissionService);

    private final Faker faker = new Faker();

    @Test
    void getContribution_should_return_contribution() {
        // Given
        final var projectId = UUID.randomUUID();
        final var contributionId = faker.pokemon().name();
        final var userId = UUID.randomUUID();
        final var githubUserId = faker.number().randomNumber();
        final var expectedContribution =
                ContributionDetailsView.builder().id(contributionId).status(ContributionStatus.COMPLETED).build();

        // When
        when(permissionService.isUserContributor(contributionId, githubUserId)).thenReturn(true);
        when(permissionService.isUserProjectLead(projectId, userId)).thenReturn(false);
        when(contributionStoragePort.findContributionById(projectId, contributionId)).thenReturn(expectedContribution);
        final var contribution = contributionService.getContribution(projectId, contributionId,
                User.builder().id(userId).githubUserId(githubUserId).build());

        // Then
        assertThat(contribution).isEqualTo(expectedContribution);
    }

    @Test
    void getContribution_should_return_contribution_when_caller_is_project_leader() {
        // Given
        final var projectId = UUID.randomUUID();
        final var contributionId = faker.pokemon().name();
        final var userId = UUID.randomUUID();
        final var githubUserId = faker.number().randomNumber();
        final var expectedContribution =
                ContributionDetailsView.builder().id(contributionId).status(ContributionStatus.COMPLETED).build();

        // When
        when(permissionService.isUserContributor(contributionId, githubUserId)).thenReturn(false);
        when(permissionService.isUserProjectLead(projectId, userId)).thenReturn(true);
        when(contributionStoragePort.findContributionById(projectId, contributionId)).thenReturn(expectedContribution);
        final var contribution = contributionService.getContribution(projectId, contributionId,
                User.builder().id(userId).githubUserId(githubUserId).build());

        // Then
        assertThat(contribution).isEqualTo(expectedContribution);
    }

    @Test
    void getContribution_should_return_401_when_caller_is_not_the_contributor_nor_a_project_leader() {
        // Given
        final var projectId = UUID.randomUUID();
        final var contributionId = faker.pokemon().name();
        final var userId = UUID.randomUUID();
        final var githubUserId = faker.number().randomNumber();

        // When
        when(permissionService.isUserContributor(contributionId, githubUserId)).thenReturn(false);
        when(permissionService.isUserProjectLead(projectId, userId)).thenReturn(false);

        assertThatThrownBy(() -> contributionService.getContribution(projectId, contributionId,
                User.builder().id(userId).githubUserId(githubUserId).build()))
                .isInstanceOf(OnlyDustException.class)
                .hasMessage("User is not the contributor of this contribution, nor a project leader of this project");
    }

    @Test
    void should_ignore_contributions() {
        // Given
        final var projectId = UUID.randomUUID();
        final var projectLeadId = UUID.randomUUID();
        final var contributionIds = List.of(faker.pokemon().name(), faker.pokemon().name());

        // When
        when(permissionService.isUserProjectLead(projectId, projectLeadId)).thenReturn(true);
        contributionService.ignoreContributions(projectId, projectLeadId, contributionIds);

        // Then
        verify(contributionStoragePort, times(1)).ignoreContributions(projectId, contributionIds);
    }

    @Test
    void should_return_401_from_ignore_contributions_when_caller_is_not_lead() {
        // Given
        final var projectId = UUID.randomUUID();
        final var projectLeadId = UUID.randomUUID();
        final var contributionIds = List.of(faker.pokemon().name(), faker.pokemon().name());

        // When
        when(permissionService.isUserProjectLead(projectId, projectLeadId)).thenReturn(false);
        assertThatThrownBy(() -> contributionService.ignoreContributions(projectId, projectLeadId,
                contributionIds))
                .isInstanceOf(OnlyDustException.class)
                .hasMessage("Only project leaders can edit the list of ignored contributions");
        // Then
        verify(contributionStoragePort, never()).ignoreContributions(projectId, contributionIds);
    }

    @Test
    void should_unignore_contributions() {
        // Given
        final var projectId = UUID.randomUUID();
        final var projectLeadId = UUID.randomUUID();
        final var contributionIds = List.of(faker.pokemon().name(), faker.pokemon().name());

        // When
        when(permissionService.isUserProjectLead(projectId, projectLeadId)).thenReturn(true);
        contributionService.unignoreContributions(projectId, projectLeadId, contributionIds);

        // Then
        verify(contributionStoragePort, times(1)).unignoreContributions(projectId, contributionIds);
    }

    @Test
    void should_return_401_from_unignore_contributions_when_caller_is_not_lead() {
        // Given
        final var projectId = UUID.randomUUID();
        final var projectLeadId = UUID.randomUUID();
        final var contributionIds = List.of(faker.pokemon().name(), faker.pokemon().name());

        // When
        when(permissionService.isUserProjectLead(projectId, projectLeadId)).thenReturn(false);
        assertThatThrownBy(() -> contributionService.unignoreContributions(projectId, projectLeadId,
                contributionIds))
                .isInstanceOf(OnlyDustException.class)
                .hasMessage("Only project leaders can edit the list of ignored contributions");
        // Then
        verify(contributionStoragePort, never()).unignoreContributions(projectId, contributionIds);
    }

    @Test
    void should_refresh_contributions() {
        // Given
        final var repoIds = List.of(faker.number().randomNumber(), faker.number().randomNumber());

        // When
        contributionService.refreshIgnoredContributions(repoIds);

        // Then
        verify(contributionStoragePort, times(1)).refreshIgnoredContributions(repoIds);
    }
}