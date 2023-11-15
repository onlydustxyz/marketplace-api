package onlydust.com.marketplace.api.domain.service;

import com.github.javafaker.Faker;
import onlydust.com.marketplace.api.domain.exception.OnlyDustException;
import onlydust.com.marketplace.api.domain.mocks.DeterministicDateProvider;
import onlydust.com.marketplace.api.domain.model.*;
import onlydust.com.marketplace.api.domain.port.output.*;
import onlydust.com.marketplace.api.domain.view.ProjectContributorsLinkView;
import onlydust.com.marketplace.api.domain.view.ProjectDetailsView;
import onlydust.com.marketplace.api.domain.view.ProjectRewardView;
import onlydust.com.marketplace.api.domain.view.RewardableItemView;
import onlydust.com.marketplace.api.domain.view.pagination.SortDirection;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

public class ProjectServiceTest {

    private final Faker faker = new Faker();
    private final DeterministicDateProvider dateProvider = new DeterministicDateProvider();

    @Test
    void should_get_a_project_by_slug() {
        // Given
        final String slug = faker.pokemon().name();
        final ProjectStoragePort projectStoragePort = mock(ProjectStoragePort.class);
        final ImageStoragePort imageStoragePort = mock(ImageStoragePort.class);
        final ProjectService projectService = new ProjectService(projectStoragePort, imageStoragePort,
                mock(UUIDGeneratorPort.class), mock(PermissionService.class), mock(IndexerPort.class), dateProvider,
                mock(EventStoragePort.class), mock(ContributionStoragePort.class),
                mock(DustyBotStoragePort.class), mock(GithubStoragePort.class));

        // When
        final var expectedProject = ProjectDetailsView.builder()
                .id(UUID.randomUUID())
                .slug(slug)
                .build();
        when(projectStoragePort.getBySlug(slug))
                .thenReturn(expectedProject);
        final var project = projectService.getBySlug(slug);

        // Then
        assertEquals(project, expectedProject);
    }

    @Test
    void should_create_project() {
        // Given
        final ProjectStoragePort projectStoragePort = mock(ProjectStoragePort.class);
        final ImageStoragePort imageStoragePort = mock(ImageStoragePort.class);
        final UUIDGeneratorPort uuidGeneratorPort = mock(UUIDGeneratorPort.class);
        final IndexerPort indexerPort = mock(IndexerPort.class);
        final EventStoragePort eventStoragePort = mock(EventStoragePort.class);
        final ProjectService projectService = new ProjectService(projectStoragePort, imageStoragePort,
                uuidGeneratorPort, mock(PermissionService.class), indexerPort, dateProvider,
                eventStoragePort, mock(ContributionStoragePort.class), mock(DustyBotStoragePort.class),
                mock(GithubStoragePort.class));
        final String imageUrl = faker.internet().image();
        final var usersToInviteAsProjectLeaders = List.of(faker.number().randomNumber());
        final CreateProjectCommand command = CreateProjectCommand.builder()
                .name(faker.pokemon().name())
                .shortDescription(faker.lorem().sentence())
                .longDescription(faker.lorem().paragraph())
                .isLookingForContributors(false)
                .moreInfos(List.of(ProjectMoreInfoLink.builder().value(faker.lorem().sentence()).url(faker.internet().url()).build()))
                .firstProjectLeaderId(UUID.randomUUID())
                .githubUserIdsAsProjectLeadersToInvite(usersToInviteAsProjectLeaders)
                .githubRepoIds(List.of(faker.number().randomNumber()))
                .imageUrl(imageUrl)
                .build();
        final UUID expectedProjectId = UUID.randomUUID();

        // When
        when(uuidGeneratorPort.generate()).thenReturn(expectedProjectId);
        when(projectStoragePort.createProject(expectedProjectId, command.getName(),
                command.getShortDescription(),
                command.getLongDescription(), command.getIsLookingForContributors(),
                command.getMoreInfos(), command.getGithubRepoIds(),
                command.getFirstProjectLeaderId(),
                command.getGithubUserIdsAsProjectLeadersToInvite(),
                ProjectVisibility.PUBLIC,
                imageUrl,
                ProjectRewardSettings.defaultSettings(dateProvider.now())
        )).thenReturn("slug");
        final var projectIdentity = projectService.createProject(command);

        // Then
        assertNotNull(projectIdentity);
        assertNotNull(projectIdentity.getLeft());
        assertThat(projectIdentity.getRight()).isEqualTo("slug");
        verify(indexerPort, times(1)).indexUsers(usersToInviteAsProjectLeaders);
        verify(eventStoragePort).saveEvent(new ProjectCreatedEvent(projectIdentity.getLeft()));
    }


    @Test
    void should_update_project() {
        // Given
        final ProjectStoragePort projectStoragePort = mock(ProjectStoragePort.class);
        final ImageStoragePort imageStoragePort = mock(ImageStoragePort.class);
        final UUIDGeneratorPort uuidGeneratorPort = mock(UUIDGeneratorPort.class);
        final PermissionService permissionService = mock(PermissionService.class);
        final IndexerPort indexerPort = mock(IndexerPort.class);
        final ContributionStoragePort contributionStoragePort = mock(ContributionStoragePort.class);
        final ProjectService projectService = new ProjectService(projectStoragePort, imageStoragePort,
                uuidGeneratorPort, permissionService, indexerPort, dateProvider, mock(EventStoragePort.class),
                contributionStoragePort, mock(DustyBotStoragePort.class), mock(GithubStoragePort.class));
        final String imageUrl = faker.internet().image();
        final var usersToInviteAsProjectLeaders = List.of(faker.number().randomNumber());
        final UUID projectId = UUID.randomUUID();
        final UUID projectLeadId = UUID.randomUUID();
        final UpdateProjectCommand command = UpdateProjectCommand.builder()
                .id(projectId)
                .name(faker.pokemon().name())
                .shortDescription(faker.lorem().sentence())
                .longDescription(faker.lorem().paragraph())
                .isLookingForContributors(false)
                .moreInfos(List.of(ProjectMoreInfoLink.builder().value(faker.lorem().sentence()).url(faker.internet().url()).build()))
                .githubUserIdsAsProjectLeadersToInvite(usersToInviteAsProjectLeaders)
                .projectLeadersToKeep(List.of(UUID.randomUUID()))
                .githubRepoIds(List.of(faker.number().randomNumber()))
                .imageUrl(imageUrl)
                .rewardSettings(
                        new ProjectRewardSettings(
                                true,
                                true,
                                false,
                                faker.date().birthday()
                        ))
                .build();

        // When
        when(permissionService.isUserProjectLead(projectId, projectLeadId)).thenReturn(true);
        projectService.updateProject(projectLeadId, command);

        // Then
        verify(indexerPort, times(1)).indexUsers(usersToInviteAsProjectLeaders);
        verify(projectStoragePort, times(1)).updateProject(command.getId(), command.getName(),
                command.getShortDescription(),
                command.getLongDescription(), command.getIsLookingForContributors(),
                command.getMoreInfos(), command.getGithubRepoIds(),
                command.getGithubUserIdsAsProjectLeadersToInvite(),
                command.getProjectLeadersToKeep(),
                imageUrl,
                command.getRewardSettings()
        );
        verify(contributionStoragePort, times(1)).refreshIgnoredContributions(projectId);
    }

    @Test
    void should_not_update_project_when_user_is_not_lead() {
        // Given
        final ProjectStoragePort projectStoragePort = mock(ProjectStoragePort.class);
        final ImageStoragePort imageStoragePort = mock(ImageStoragePort.class);
        final UUIDGeneratorPort uuidGeneratorPort = mock(UUIDGeneratorPort.class);
        final PermissionService permissionService = mock(PermissionService.class);
        final IndexerPort indexerPort = mock(IndexerPort.class);
        final ProjectService projectService = new ProjectService(projectStoragePort, imageStoragePort,
                uuidGeneratorPort, permissionService, indexerPort, dateProvider, mock(EventStoragePort.class),
                mock(ContributionStoragePort.class), mock(DustyBotStoragePort.class), mock(GithubStoragePort.class));
        final String imageUrl = faker.internet().image();
        final var usersToInviteAsProjectLeaders = List.of(faker.number().randomNumber());
        final UUID projectId = UUID.randomUUID();
        final UUID projectLeadId = UUID.randomUUID();
        final UpdateProjectCommand command = UpdateProjectCommand.builder()
                .id(projectId)
                .name(faker.pokemon().name())
                .shortDescription(faker.lorem().sentence())
                .longDescription(faker.lorem().paragraph())
                .isLookingForContributors(false)
                .moreInfos(List.of(ProjectMoreInfoLink.builder().value(faker.lorem().sentence()).url(faker.internet().url()).build()))
                .githubUserIdsAsProjectLeadersToInvite(usersToInviteAsProjectLeaders)
                .projectLeadersToKeep(List.of(UUID.randomUUID()))
                .githubRepoIds(List.of(faker.number().randomNumber()))
                .imageUrl(imageUrl)
                .rewardSettings(
                        new ProjectRewardSettings(
                                true,
                                true,
                                false,
                                faker.date().birthday()
                        ))
                .build();

        // When
        when(permissionService.isUserProjectLead(projectId, projectLeadId)).thenReturn(false);

        // Then
        assertThatThrownBy(() -> projectService.updateProject(projectLeadId, command))
                .isInstanceOf(OnlyDustException.class)
                .hasMessage("Only project leads can update their projects");
    }

    @Test
    void should_upload_logo() throws MalformedURLException {
        // Given
        final ProjectStoragePort projectStoragePort = mock(ProjectStoragePort.class);
        final ImageStoragePort imageStoragePort = mock(ImageStoragePort.class);
        final UUIDGeneratorPort uuidGeneratorPort = mock(UUIDGeneratorPort.class);
        final ProjectService projectService = new ProjectService(projectStoragePort, imageStoragePort,
                uuidGeneratorPort, mock(PermissionService.class), mock(IndexerPort.class), dateProvider,
                mock(EventStoragePort.class), mock(ContributionStoragePort.class), mock(DustyBotStoragePort.class),
                mock(GithubStoragePort.class));
        final InputStream imageInputStream = mock(InputStream.class);
        final String imageUrl = faker.internet().image();

        // When
        when(imageStoragePort.storeImage(imageInputStream)).thenReturn(new URL(imageUrl));
        final URL url = projectService.saveLogoImage(imageInputStream);

        // Then
        assertThat(url.toString()).isEqualTo(imageUrl);
    }

    @Test
    void should_check_project_lead_permissions_when_getting_project_contributors_given_an_invalid_project_lead() {
        // Given
        final ProjectStoragePort projectStoragePort = mock(ProjectStoragePort.class);
        final ContributionStoragePort contributionStoragePort = mock(ContributionStoragePort.class);
        final PermissionService permissionService = new PermissionService(projectStoragePort, contributionStoragePort);
        final ProjectService projectService = new ProjectService(projectStoragePort, mock(ImageStoragePort.class),
                mock(UUIDGeneratorPort.class), permissionService, mock(IndexerPort.class), dateProvider,
                mock(EventStoragePort.class), mock(ContributionStoragePort.class), mock(DustyBotStoragePort.class),
                mock(GithubStoragePort.class));
        final UUID projectId = UUID.randomUUID();
        final ProjectContributorsLinkView.SortBy sortBy = ProjectContributorsLinkView.SortBy.login;
        final UUID projectLeadId = UUID.randomUUID();
        final int pageIndex = 1;
        final int pageSize = 1;
        final SortDirection sortDirection = SortDirection.asc;

        // When
        when(projectStoragePort.getProjectLeadIds(projectId))
                .thenReturn(List.of(UUID.randomUUID()));
        projectService.getContributorsForProjectLeadId(projectId, null, projectLeadId, sortBy, sortDirection, pageIndex,
                pageSize);

        // Then
        verify(projectStoragePort, times(1)).findContributors(projectId, null, sortBy, sortDirection, pageIndex,
                pageSize);
        verify(projectStoragePort, times(0)).findContributorsForProjectLead(projectId, null, sortBy, sortDirection,
                pageIndex, pageSize);
    }

    @Test
    void should_check_project_lead_permissions_when_getting_project_contributors_given_a_valid_project_lead() {
        // Given
        final ProjectStoragePort projectStoragePort = mock(ProjectStoragePort.class);
        final ContributionStoragePort contributionStoragePort = mock(ContributionStoragePort.class);
        final PermissionService permissionService = new PermissionService(projectStoragePort, contributionStoragePort);
        final ProjectService projectService = new ProjectService(projectStoragePort, mock(ImageStoragePort.class),
                mock(UUIDGeneratorPort.class), permissionService, mock(IndexerPort.class), dateProvider,
                mock(EventStoragePort.class), mock(ContributionStoragePort.class), mock(DustyBotStoragePort.class),
                mock(GithubStoragePort.class));
        final UUID projectId = UUID.randomUUID();
        final ProjectContributorsLinkView.SortBy sortBy = ProjectContributorsLinkView.SortBy.login;
        final String login = faker.name().username();
        final UUID projectLeadId = UUID.randomUUID();
        final int pageIndex = 1;
        final int pageSize = 1;
        final SortDirection sortDirection = SortDirection.desc;

        // When
        when(projectStoragePort.getProjectLeadIds(projectId))
                .thenReturn(List.of(UUID.randomUUID(), projectLeadId));
        projectService.getContributorsForProjectLeadId(projectId, login, projectLeadId, sortBy, sortDirection,
                pageIndex,
                pageSize);

        // Then
        verify(projectStoragePort, times(0)).findContributors(projectId, login, sortBy, sortDirection, pageIndex,
                pageSize);
        verify(projectStoragePort, times(1)).findContributorsForProjectLead(projectId, login, sortBy, sortDirection,
                pageIndex, pageSize);
    }


    @Test
    void should_check_project_lead_permissions_when_getting_project_rewards_given_a_valid_project_lead() {
        final ProjectStoragePort projectStoragePort = mock(ProjectStoragePort.class);
        final ContributionStoragePort contributionStoragePort = mock(ContributionStoragePort.class);
        final PermissionService permissionService = new PermissionService(projectStoragePort, contributionStoragePort);
        final ProjectService projectService = new ProjectService(projectStoragePort, mock(ImageStoragePort.class),
                mock(UUIDGeneratorPort.class), permissionService, mock(IndexerPort.class), dateProvider,
                mock(EventStoragePort.class), mock(ContributionStoragePort.class), mock(DustyBotStoragePort.class),
                mock(GithubStoragePort.class));
        final UUID projectId = UUID.randomUUID();
        final ProjectRewardView.SortBy sortBy = ProjectRewardView.SortBy.contribution;
        final UUID projectLeadId = UUID.randomUUID();
        final int pageIndex = 1;
        final int pageSize = 1;
        final SortDirection sortDirection = SortDirection.desc;

        // When
        when(projectStoragePort.getProjectLeadIds(projectId))
                .thenReturn(List.of(UUID.randomUUID(), projectLeadId));
        projectService.getRewards(projectId, projectLeadId, pageIndex, pageSize, sortBy, sortDirection);

        // Then
        verify(projectStoragePort, times(1)).findRewards(projectId, sortBy, sortDirection, pageIndex, pageSize);
    }

    @Test
    void should_throw_forbidden_exception_when_getting_project_rewards_given_an_invalid_project_lead() {
        final ProjectStoragePort projectStoragePort = mock(ProjectStoragePort.class);
        final ContributionStoragePort contributionStoragePort = mock(ContributionStoragePort.class);
        final PermissionService permissionService = new PermissionService(projectStoragePort, contributionStoragePort);
        final ProjectService projectService = new ProjectService(projectStoragePort, mock(ImageStoragePort.class),
                mock(UUIDGeneratorPort.class), permissionService, mock(IndexerPort.class), dateProvider,
                mock(EventStoragePort.class), mock(ContributionStoragePort.class), mock(DustyBotStoragePort.class),
                mock(GithubStoragePort.class));
        final UUID projectId = UUID.randomUUID();
        final ProjectRewardView.SortBy sortBy = ProjectRewardView.SortBy.contribution;
        final int pageIndex = 1;
        final int pageSize = 1;
        final SortDirection sortDirection = SortDirection.asc;

        // When
        when(projectStoragePort.getProjectLeadIds(projectId))
                .thenReturn(List.of(UUID.randomUUID()));
        OnlyDustException onlyDustException = null;
        try {
            projectService.getRewards(projectId, UUID.randomUUID(), pageIndex, pageSize, sortBy, sortDirection);
        } catch (OnlyDustException e) {
            onlyDustException = e;
        }

        // Then
        verify(projectStoragePort, times(0)).findRewards(projectId, sortBy, sortDirection, pageIndex, pageSize);
        assertNotNull(onlyDustException);
        assertEquals(403, onlyDustException.getStatus());
        assertEquals("Only project leads can read rewards on their projects", onlyDustException.getMessage());
    }


    @Test
    void should_check_project_lead_permissions_when_getting_project_budgets_given_a_valid_project_lead() {
        final ProjectStoragePort projectStoragePort = mock(ProjectStoragePort.class);
        final ContributionStoragePort contributionStoragePort = mock(ContributionStoragePort.class);
        final PermissionService permissionService = new PermissionService(projectStoragePort, contributionStoragePort);
        final ProjectService projectService = new ProjectService(projectStoragePort, mock(ImageStoragePort.class),
                mock(UUIDGeneratorPort.class), permissionService, mock(IndexerPort.class), dateProvider,
                mock(EventStoragePort.class), mock(ContributionStoragePort.class), mock(DustyBotStoragePort.class),
                mock(GithubStoragePort.class));
        final UUID projectId = UUID.randomUUID();
        final UUID projectLeadId = UUID.randomUUID();

        // When
        when(projectStoragePort.getProjectLeadIds(projectId))
                .thenReturn(List.of(UUID.randomUUID(), projectLeadId));
        projectService.getBudgets(projectId, projectLeadId);

        // Then
        verify(projectStoragePort, times(1)).findBudgets(projectId);
    }

    @Test
    void should_throw_forbidden_exception_when_getting_project_budgets_given_an_invalid_project_lead() {
        final ProjectStoragePort projectStoragePort = mock(ProjectStoragePort.class);
        final ContributionStoragePort contributionStoragePort = mock(ContributionStoragePort.class);
        final PermissionService permissionService = new PermissionService(projectStoragePort, contributionStoragePort);
        final ProjectService projectService = new ProjectService(projectStoragePort, mock(ImageStoragePort.class),
                mock(UUIDGeneratorPort.class), permissionService, mock(IndexerPort.class), dateProvider,
                mock(EventStoragePort.class), mock(ContributionStoragePort.class), mock(DustyBotStoragePort.class),
                mock(GithubStoragePort.class));
        final UUID projectId = UUID.randomUUID();

        // When
        when(projectStoragePort.getProjectLeadIds(projectId))
                .thenReturn(List.of(UUID.randomUUID()));
        OnlyDustException onlyDustException = null;
        try {
            projectService.getBudgets(projectId, UUID.randomUUID());
        } catch (OnlyDustException e) {
            onlyDustException = e;
        }

        // Then
        verify(projectStoragePort, times(0)).findBudgets(projectId);
        assertNotNull(onlyDustException);
        assertEquals(403, onlyDustException.getStatus());
        assertEquals("Only project leads can read budgets on their projects", onlyDustException.getMessage());
    }


    @Test
    void should_check_project_lead_permissions_when_getting_project_reward_by_id_given_a_valid_project_lead() {
        final ProjectStoragePort projectStoragePort = mock(ProjectStoragePort.class);
        final ContributionStoragePort contributionStoragePort = mock(ContributionStoragePort.class);
        final PermissionService permissionService = new PermissionService(projectStoragePort, contributionStoragePort);
        final ProjectService projectService = new ProjectService(projectStoragePort, mock(ImageStoragePort.class),
                mock(UUIDGeneratorPort.class), permissionService, mock(IndexerPort.class), dateProvider,
                mock(EventStoragePort.class), mock(ContributionStoragePort.class), mock(DustyBotStoragePort.class),
                mock(GithubStoragePort.class));
        final UUID projectId = UUID.randomUUID();
        final UUID projectLeadId = UUID.randomUUID();
        final UUID rewardId = UUID.randomUUID();

        // When
        when(projectStoragePort.getProjectLeadIds(projectId))
                .thenReturn(List.of(UUID.randomUUID(), projectLeadId));
        projectService.getRewardByIdForProjectLead(projectId, rewardId, projectLeadId);

        // Then
        verify(projectStoragePort, times(1)).getProjectReward(rewardId);
    }

    @Test
    void should_throw_forbidden_exception_when_getting_project_reward_by_id_given_an_invalid_project_lead() {
        final ProjectStoragePort projectStoragePort = mock(ProjectStoragePort.class);
        final ContributionStoragePort contributionStoragePort = mock(ContributionStoragePort.class);
        final PermissionService permissionService = new PermissionService(projectStoragePort, contributionStoragePort);
        final ProjectService projectService = new ProjectService(projectStoragePort, mock(ImageStoragePort.class),
                mock(UUIDGeneratorPort.class), permissionService, mock(IndexerPort.class), dateProvider,
                mock(EventStoragePort.class), mock(ContributionStoragePort.class), mock(DustyBotStoragePort.class),
                mock(GithubStoragePort.class));
        final UUID projectId = UUID.randomUUID();
        final UUID rewardId = UUID.randomUUID();

        // When
        when(projectStoragePort.getProjectLeadIds(projectId))
                .thenReturn(List.of(UUID.randomUUID()));
        OnlyDustException onlyDustException = null;
        try {
            projectService.getRewardByIdForProjectLead(projectId, rewardId, UUID.randomUUID());
        } catch (OnlyDustException e) {
            onlyDustException = e;
        }

        // Then
        verify(projectStoragePort, times(0)).findBudgets(projectId);
        assertNotNull(onlyDustException);
        assertEquals(403, onlyDustException.getStatus());
        assertEquals("Only project leads can read reward on their projects", onlyDustException.getMessage());
    }

    @Test
    void should_check_project_lead_permissions_when_getting_project_reward_items_by_id_given_a_valid_project_lead() {
        final ProjectStoragePort projectStoragePort = mock(ProjectStoragePort.class);
        final ContributionStoragePort contributionStoragePort = mock(ContributionStoragePort.class);
        final PermissionService permissionService = new PermissionService(projectStoragePort, contributionStoragePort);
        final ProjectService projectService = new ProjectService(projectStoragePort, mock(ImageStoragePort.class),
                mock(UUIDGeneratorPort.class), permissionService, mock(IndexerPort.class), dateProvider,
                mock(EventStoragePort.class), mock(ContributionStoragePort.class), mock(DustyBotStoragePort.class),
                mock(GithubStoragePort.class));
        final UUID projectId = UUID.randomUUID();
        final UUID projectLeadId = UUID.randomUUID();
        final UUID rewardId = UUID.randomUUID();

        // When
        when(projectStoragePort.getProjectLeadIds(projectId))
                .thenReturn(List.of(UUID.randomUUID(), projectLeadId));
        projectService.getRewardItemsPageByIdForProjectLead(projectId, rewardId, projectLeadId, 0, 50);

        // Then
        verify(projectStoragePort, times(1)).getProjectRewardItems(rewardId, 0, 50);
    }

    @Test
    void should_throw_forbidden_exception_when_getting_project_reward_items_by_id_given_an_invalid_project_lead() {
        final ProjectStoragePort projectStoragePort = mock(ProjectStoragePort.class);
        final ContributionStoragePort contributionStoragePort = mock(ContributionStoragePort.class);
        final PermissionService permissionService = new PermissionService(projectStoragePort, contributionStoragePort);
        final ProjectService projectService = new ProjectService(projectStoragePort, mock(ImageStoragePort.class),
                mock(UUIDGeneratorPort.class), permissionService, mock(IndexerPort.class), dateProvider,
                mock(EventStoragePort.class), mock(ContributionStoragePort.class), mock(DustyBotStoragePort.class),
                mock(GithubStoragePort.class));
        final UUID projectId = UUID.randomUUID();
        final UUID rewardId = UUID.randomUUID();

        // When
        when(projectStoragePort.getProjectLeadIds(projectId))
                .thenReturn(List.of(UUID.randomUUID()));
        OnlyDustException onlyDustException = null;
        try {
            projectService.getRewardItemsPageByIdForProjectLead(projectId, rewardId, UUID.randomUUID(), 0, 50);
        } catch (OnlyDustException e) {
            onlyDustException = e;
        }

        // Then
        verify(projectStoragePort, times(0)).findBudgets(projectId);
        assertNotNull(onlyDustException);
        assertEquals(403, onlyDustException.getStatus());
        assertEquals("Only project leads can read reward items on their projects", onlyDustException.getMessage());
    }

    @Test
    void should_check_project_lead_permissions_when_getting_project_rewardable_items_by_id_given_a_valid_project_lead() {
        final ProjectStoragePort projectStoragePort = mock(ProjectStoragePort.class);
        final ContributionStoragePort contributionStoragePort = mock(ContributionStoragePort.class);
        final PermissionService permissionService = new PermissionService(projectStoragePort, contributionStoragePort);
        final ProjectService projectService = new ProjectService(projectStoragePort, mock(ImageStoragePort.class),
                mock(UUIDGeneratorPort.class), permissionService, mock(IndexerPort.class), dateProvider,
                mock(EventStoragePort.class), mock(ContributionStoragePort.class), mock(DustyBotStoragePort.class),
                mock(GithubStoragePort.class));
        final UUID projectId = UUID.randomUUID();
        final UUID projectLeadId = UUID.randomUUID();

        // When
        when(projectStoragePort.getProjectLeadIds(projectId))
                .thenReturn(List.of(UUID.randomUUID(), projectLeadId));
        projectService.getRewardableItemsPageByTypeForProjectLeadAndContributorId(projectId, null, projectLeadId,
                12345L, 0, 50, null, null);

        // Then
        verify(projectStoragePort, times(1)).getProjectRewardableItemsByTypeForProjectLeadAndContributorId(projectId,
                null, 12345L, 0, 50, null, null);
    }

    @Test
    void should_throw_forbidden_exception_when_getting_project_rewardable_items_by_id_given_an_invalid_project_lead() {
        final ProjectStoragePort projectStoragePort = mock(ProjectStoragePort.class);
        final ContributionStoragePort contributionStoragePort = mock(ContributionStoragePort.class);
        final PermissionService permissionService = new PermissionService(projectStoragePort, contributionStoragePort);
        final ProjectService projectService = new ProjectService(projectStoragePort, mock(ImageStoragePort.class),
                mock(UUIDGeneratorPort.class), permissionService, mock(IndexerPort.class), dateProvider,
                mock(EventStoragePort.class), mock(ContributionStoragePort.class), mock(DustyBotStoragePort.class),
                mock(GithubStoragePort.class));
        final UUID projectId = UUID.randomUUID();

        // When
        when(projectStoragePort.getProjectLeadIds(projectId))
                .thenReturn(List.of(UUID.randomUUID()));
        OnlyDustException onlyDustException = null;
        try {
            projectService.getRewardableItemsPageByTypeForProjectLeadAndContributorId(projectId, null,
                    UUID.randomUUID(), 1234L, 0, 50, null, null);
        } catch (OnlyDustException e) {
            onlyDustException = e;
        }

        // Then
        verify(projectStoragePort, times(0)).findBudgets(projectId);
        assertNotNull(onlyDustException);
        assertEquals(403, onlyDustException.getStatus());
        assertEquals("Only project leads can read rewardable items on their projects", onlyDustException.getMessage());
    }

    @Test
    void should_throw_forbidden_exception_when_creating_rewardable_issue_given_an_invalid_project_lead() {
        final ProjectStoragePort projectStoragePort = mock(ProjectStoragePort.class);
        final ContributionStoragePort contributionStoragePort = mock(ContributionStoragePort.class);
        final PermissionService permissionService = new PermissionService(projectStoragePort, contributionStoragePort);
        final DustyBotStoragePort dustyBotStoragePort = mock(DustyBotStoragePort.class);
        final ProjectService projectService = new ProjectService(projectStoragePort, mock(ImageStoragePort.class),
                mock(UUIDGeneratorPort.class), permissionService, mock(IndexerPort.class), dateProvider,
                mock(EventStoragePort.class), mock(ContributionStoragePort.class), dustyBotStoragePort,
                mock(GithubStoragePort.class));
        final UUID projectId = UUID.randomUUID();
        final UUID projectLeadId = UUID.randomUUID();

        // When
        when(projectStoragePort.getProjectLeadIds(projectId))
                .thenReturn(List.of(UUID.randomUUID()));
        OnlyDustException onlyDustException = null;
        try {
            projectService.createAndCloseIssueForProjectIdAndRepositoryId(CreateAndCloseIssueCommand.builder()
                    .projectId(projectId)
                    .projectLeadId(projectLeadId)
                    .build());
        } catch (OnlyDustException e) {
            onlyDustException = e;
        }

        // Then
        verifyNoInteractions(dustyBotStoragePort);
        assertNotNull(onlyDustException);
        assertEquals(403, onlyDustException.getStatus());
        assertEquals("Only project leads can create rewardable issue on their projects",
                onlyDustException.getMessage());
    }

    @Test
    void should_throw_forbidden_exception_when_creating_rewardable_issue_given_a_valid_project_lead_and_invalid_github_repo_id() {
        final ProjectStoragePort projectStoragePort = mock(ProjectStoragePort.class);
        final ContributionStoragePort contributionStoragePort = mock(ContributionStoragePort.class);
        final PermissionService permissionService = new PermissionService(projectStoragePort, contributionStoragePort);
        final DustyBotStoragePort dustyBotStoragePort = mock(DustyBotStoragePort.class);
        final ProjectService projectService = new ProjectService(projectStoragePort, mock(ImageStoragePort.class),
                mock(UUIDGeneratorPort.class), permissionService, mock(IndexerPort.class), dateProvider,
                mock(EventStoragePort.class), mock(ContributionStoragePort.class), dustyBotStoragePort,
                mock(GithubStoragePort.class));
        final UUID projectId = UUID.randomUUID();
        final UUID projectLeadId = UUID.randomUUID();
        final Long githubRepoId = 1L;

        // When
        when(projectStoragePort.getProjectLeadIds(projectId))
                .thenReturn(List.of(projectLeadId));
        when(projectStoragePort.getProjectRepoIds(projectId))
                .thenReturn(Set.of(2L, 3L));
        OnlyDustException onlyDustException = null;
        try {
            projectService.createAndCloseIssueForProjectIdAndRepositoryId(CreateAndCloseIssueCommand.builder()
                    .projectId(projectId)
                    .projectLeadId(projectLeadId)
                    .githubRepoId(githubRepoId)
                    .build());
        } catch (OnlyDustException e) {
            onlyDustException = e;
        }

        // Then
        verifyNoInteractions(dustyBotStoragePort);
        assertNotNull(onlyDustException);
        assertEquals(403, onlyDustException.getStatus());
        assertEquals("Rewardable issue can only be created on repos linked to this project",
                onlyDustException.getMessage());
    }

    @Test
    void should_create_and_close_rewardable_issue_given_a_valid_project_lead_and_valid_github_repo_id() {
        final ProjectStoragePort projectStoragePort = mock(ProjectStoragePort.class);
        final ContributionStoragePort contributionStoragePort = mock(ContributionStoragePort.class);
        final PermissionService permissionService = new PermissionService(projectStoragePort, contributionStoragePort);
        final DustyBotStoragePort dustyBotStoragePort = mock(DustyBotStoragePort.class);
        final GithubStoragePort githubStoragePort = mock(GithubStoragePort.class);
        final ProjectService projectService = new ProjectService(projectStoragePort, mock(ImageStoragePort.class),
                mock(UUIDGeneratorPort.class), permissionService, mock(IndexerPort.class), dateProvider,
                mock(EventStoragePort.class), mock(ContributionStoragePort.class), dustyBotStoragePort,
                githubStoragePort);
        final UUID projectId = UUID.randomUUID();
        final UUID projectLeadId = UUID.randomUUID();
        final Long githubRepoId = 1L;
        final String githubRepoOwner = faker.name().username();
        final String githubRepoName = faker.pokemon().name();
        final String title = faker.pokemon().name();
        final String description = faker.lorem().paragraph();
        final Long issueNumber = 1234L;

        // When
        when(projectStoragePort.getProjectLeadIds(projectId))
                .thenReturn(List.of(projectLeadId));
        when(projectStoragePort.getProjectRepoIds(projectId))
                .thenReturn(Set.of(2L, 3L, githubRepoId));
        when(githubStoragePort.findRepoById(githubRepoId))
                .thenReturn(Optional.of(GithubRepo.builder()
                        .id(githubRepoId)
                        .owner(githubRepoOwner)
                        .name(githubRepoName)
                        .build()));
        when(dustyBotStoragePort.createIssue(githubRepoOwner, githubRepoName, title, description))
                .thenReturn(RewardableItemView.builder().number(issueNumber).build());
        when(dustyBotStoragePort.closeIssue(githubRepoOwner, githubRepoName, issueNumber))
                .thenReturn(RewardableItemView.builder().number(issueNumber).build());

        final CreateAndCloseIssueCommand createAndCloseIssueCommand = CreateAndCloseIssueCommand.builder()
                .projectId(projectId)
                .projectLeadId(projectLeadId)
                .githubRepoId(githubRepoId)
                .title(title)
                .description(description)
                .build();
        projectService.createAndCloseIssueForProjectIdAndRepositoryId(createAndCloseIssueCommand);

        // Then
        verify(dustyBotStoragePort, times(1)).createIssue(githubRepoOwner, githubRepoName, title, description);
        verify(dustyBotStoragePort, times(1)).closeIssue(githubRepoOwner, githubRepoName, issueNumber);
    }

    @Test
    void should_add_rewardable_issue() {
        final ProjectStoragePort projectStoragePort = mock(ProjectStoragePort.class);
        final PermissionService permissionService = new PermissionService(projectStoragePort,
                mock(ContributionStoragePort.class));
        final IndexerPort indexerPort = mock(IndexerPort.class);
        final ProjectService projectService = new ProjectService(projectStoragePort, mock(ImageStoragePort.class),
                mock(UUIDGeneratorPort.class), permissionService, indexerPort, dateProvider,
                mock(EventStoragePort.class), mock(ContributionStoragePort.class), mock(DustyBotStoragePort.class),
                mock(GithubStoragePort.class));
        final UUID projectId = UUID.randomUUID();
        final UUID projectLeadId = UUID.randomUUID();
        final String githubRepoOwner = faker.name().username();
        final String githubRepoName = faker.pokemon().name();
        final Long issueNumber = 1234L;

        // When
        when(projectStoragePort.getProjectLeadIds(projectId))
                .thenReturn(List.of(projectLeadId));
        when(projectStoragePort.getRewardableIssue(githubRepoOwner, githubRepoName, issueNumber))
                .thenReturn(RewardableItemView.builder().number(issueNumber).build());

        projectService.addRewardableIssue(projectId, projectLeadId, "https://github.com/%s/%s/issues/%d".formatted(
                githubRepoOwner, githubRepoName, issueNumber));

        // Then
        verify(indexerPort, times(1)).indexIssue(githubRepoOwner, githubRepoName, issueNumber);
    }

    @Test
    void should_reject_invalid_issue_url() {
        final ProjectStoragePort projectStoragePort = mock(ProjectStoragePort.class);
        final PermissionService permissionService = new PermissionService(projectStoragePort,
                mock(ContributionStoragePort.class));
        final IndexerPort indexerPort = mock(IndexerPort.class);
        final ProjectService projectService = new ProjectService(projectStoragePort, mock(ImageStoragePort.class),
                mock(UUIDGeneratorPort.class), permissionService, indexerPort, dateProvider,
                mock(EventStoragePort.class), mock(ContributionStoragePort.class), mock(DustyBotStoragePort.class),
                mock(GithubStoragePort.class));
        final UUID projectId = UUID.randomUUID();
        final UUID projectLeadId = UUID.randomUUID();
        final String githubRepoOwner = faker.name().username();
        final String githubRepoName = faker.pokemon().name();

        // When
        when(projectStoragePort.getProjectLeadIds(projectId))
                .thenReturn(List.of(projectLeadId));

        assertThatThrownBy(() -> projectService.addRewardableIssue(projectId,
                projectLeadId,
                "https://github.com/%s/%s/issues".formatted(githubRepoOwner, githubRepoName)))
                .isInstanceOf(OnlyDustException.class);

        // Then
        verify(indexerPort, never()).indexIssue(anyString(), anyString(), anyLong());
        verify(projectStoragePort, never()).getRewardableIssue(anyString(), anyString(), anyLong());
    }


    @Test
    void should_add_rewardable_pull_request() {
        final ProjectStoragePort projectStoragePort = mock(ProjectStoragePort.class);
        final PermissionService permissionService = new PermissionService(projectStoragePort,
                mock(ContributionStoragePort.class));
        final IndexerPort indexerPort = mock(IndexerPort.class);
        final ProjectService projectService = new ProjectService(projectStoragePort, mock(ImageStoragePort.class),
                mock(UUIDGeneratorPort.class), permissionService, indexerPort, dateProvider,
                mock(EventStoragePort.class), mock(ContributionStoragePort.class), mock(DustyBotStoragePort.class),
                mock(GithubStoragePort.class));
        final UUID projectId = UUID.randomUUID();
        final UUID projectLeadId = UUID.randomUUID();
        final String githubRepoOwner = faker.name().username();
        final String githubRepoName = faker.pokemon().name();
        final Long pullRequestNumber = 1234L;

        // When
        when(projectStoragePort.getProjectLeadIds(projectId))
                .thenReturn(List.of(projectLeadId));
        when(projectStoragePort.getRewardablePullRequest(githubRepoOwner, githubRepoName, pullRequestNumber))
                .thenReturn(RewardableItemView.builder().number(pullRequestNumber).build());

        projectService.addRewardablePullRequest(projectId, projectLeadId, "https://github.com/%s/%s/pull/%d".formatted(
                githubRepoOwner, githubRepoName, pullRequestNumber));

        // Then
        verify(indexerPort, times(1)).indexPullRequest(githubRepoOwner, githubRepoName, pullRequestNumber);
    }

    @Test
    void should_reject_invalid_pull_request_url() {
        final ProjectStoragePort projectStoragePort = mock(ProjectStoragePort.class);
        final PermissionService permissionService = new PermissionService(projectStoragePort,
                mock(ContributionStoragePort.class));
        final IndexerPort indexerPort = mock(IndexerPort.class);
        final ProjectService projectService = new ProjectService(projectStoragePort, mock(ImageStoragePort.class),
                mock(UUIDGeneratorPort.class), permissionService, indexerPort, dateProvider,
                mock(EventStoragePort.class), mock(ContributionStoragePort.class), mock(DustyBotStoragePort.class),
                mock(GithubStoragePort.class));
        final UUID projectId = UUID.randomUUID();
        final UUID projectLeadId = UUID.randomUUID();
        final String githubRepoOwner = faker.name().username();
        final String githubRepoName = faker.pokemon().name();

        // When
        when(projectStoragePort.getProjectLeadIds(projectId))
                .thenReturn(List.of(projectLeadId));

        assertThatThrownBy(() -> projectService.addRewardablePullRequest(projectId,
                projectLeadId,
                "https://github.com/%s/%s/pull".formatted(githubRepoOwner, githubRepoName)))
                .isInstanceOf(OnlyDustException.class);

        // Then
        verify(indexerPort, never()).indexPullRequest(anyString(), anyString(), anyLong());
        verify(projectStoragePort, never()).getRewardablePullRequest(anyString(), anyString(), anyLong());
    }
}
