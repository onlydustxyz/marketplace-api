package onlydust.com.marketplace.project.domain.service;

import com.github.javafaker.Faker;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import onlydust.com.marketplace.project.domain.model.*;
import onlydust.com.marketplace.project.domain.port.output.ContributionStoragePort;
import onlydust.com.marketplace.project.domain.port.output.GithubApiPort;
import onlydust.com.marketplace.project.domain.view.ContributionDetailsView;
import onlydust.com.marketplace.project.domain.view.ContributorLinkView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class ContributionServiceTest {

    final ContributionStoragePort contributionStoragePort = mock(ContributionStoragePort.class);
    final PermissionService permissionService = mock(PermissionService.class);
    final GithubAppService githubAppService = mock(GithubAppService.class);
    final GithubApiPort githubApiPort = mock(GithubApiPort.class);

    final ContributionService contributionService = new ContributionService(contributionStoragePort, permissionService, githubAppService, githubApiPort);

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

    @Nested
    class UnassignContribution {
        final UUID callerId = UUID.randomUUID();

        final Project project = Project.builder()
                .id(UUID.randomUUID())
                .build();

        final GithubRepo repo = GithubRepo.builder()
                .id(faker.random().nextLong())
                .build();

        final ContributorLinkView contributor = ContributorLinkView.builder()
                .login(faker.name().username())
                .build();

        final ContributionDetailsView contribution = ContributionDetailsView.builder()
                .id(faker.random().hex(26))
                .project(project)
                .type(ContributionType.ISSUE)
                .status(ContributionStatus.IN_PROGRESS)
                .githubRepo(repo)
                .contributor(contributor)
                .build();

        final GithubAppAccessToken githubToken = new GithubAppAccessToken(faker.internet().password(), Map.of("issues", "write"));

        @BeforeEach
        void setUp() {
            reset(permissionService, contributionStoragePort);
            when(permissionService.isUserProjectLead(project.getId(), callerId)).thenReturn(true);
            when(contributionStoragePort.findContributionById(project.getId(), contribution.getId())).thenReturn(contribution);
            when(githubAppService.getInstallationTokenFor(repo.getId())).thenReturn(Optional.of(githubToken));
        }

        @Test
        void should_prevent_unassigning_contribution_when_caller_is_not_lead() {
            // Given
            when(permissionService.isUserProjectLead(project.getId(), callerId)).thenReturn(false);

            // When
            assertThatThrownBy(() -> contributionService.unassign(project.getId(), callerId, contribution.getId()))
                    // Then
                    .isInstanceOf(OnlyDustException.class)
                    .hasMessage("Only project leaders can unassign contributions");
        }

        @Test
        void should_prevent_unassigning_contribution_if_not_an_issue() {
            // Given
            when(contributionStoragePort.findContributionById(project.getId(), contribution.getId())).thenReturn(contribution.toBuilder()
                    .type(ContributionType.PULL_REQUEST)
                    .build());

            // When
            assertThatThrownBy(() -> contributionService.unassign(project.getId(), callerId, contribution.getId()))
                    // Then
                    .isInstanceOf(OnlyDustException.class)
                    .hasMessage("Only issues can be unassigned");
        }

        @ParameterizedTest
        @EnumSource(value = ContributionStatus.class, names = {"IN_PROGRESS"}, mode = EnumSource.Mode.EXCLUDE)
        void should_prevent_unassigning_contribution_if_not_in_progress(ContributionStatus status) {
            // Given
            when(contributionStoragePort.findContributionById(project.getId(), contribution.getId())).thenReturn(contribution.toBuilder()
                    .status(status)
                    .build());

            // When
            assertThatThrownBy(() -> contributionService.unassign(project.getId(), callerId, contribution.getId()))
                    // Then
                    .isInstanceOf(OnlyDustException.class)
                    .hasMessage("Only in progress contributions can be unassigned");
        }

        @Test
        void should_prevent_unassigning_contribution_if_github_app_not_installed() {
            // Given
            when(githubAppService.getInstallationTokenFor(repo.getId())).thenReturn(Optional.empty());

            // When
            assertThatThrownBy(() -> contributionService.unassign(project.getId(), callerId, contribution.getId()))
                    // Then
                    .isInstanceOf(OnlyDustException.class)
                    .hasMessage("Could not to generate installation token for GitHub repo %d".formatted(repo.getId()));
        }

        @Test
        void should_unassign_contribution() {
            // When
            contributionService.unassign(project.getId(), callerId, contribution.getId());

            // Then
            verify(githubApiPort).unassign(githubToken.token(), repo.getId(), contribution.getGithubNumber(), contribution.getContributor().getLogin());
        }
    }
}