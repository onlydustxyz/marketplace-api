package onlydust.com.marketplace.project.domain.service;

import com.github.javafaker.Faker;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import onlydust.com.marketplace.kernel.model.AuthenticatedUser;
import onlydust.com.marketplace.kernel.model.OrSlug;
import onlydust.com.marketplace.kernel.model.ProjectId;
import onlydust.com.marketplace.kernel.model.UserId;
import onlydust.com.marketplace.kernel.pagination.Page;
import onlydust.com.marketplace.kernel.pagination.SortDirection;
import onlydust.com.marketplace.kernel.port.output.ImageStoragePort;
import onlydust.com.marketplace.kernel.port.output.IndexerPort;
import onlydust.com.marketplace.project.domain.mocks.DeterministicDateProvider;
import onlydust.com.marketplace.project.domain.model.*;
import onlydust.com.marketplace.project.domain.port.input.ProjectObserverPort;
import onlydust.com.marketplace.project.domain.port.output.*;
import onlydust.com.marketplace.project.domain.view.ContributionView;
import onlydust.com.marketplace.project.domain.view.RewardableItemView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

public class ProjectServiceTest {

    private final Faker faker = new Faker();
    private final DeterministicDateProvider dateProvider = new DeterministicDateProvider();
    private final ProjectStoragePort projectStoragePort = mock(ProjectStoragePort.class);
    private final ContributionStoragePort contributionStoragePort = mock(ContributionStoragePort.class);
    private final PermissionService permissionService = mock(PermissionService.class);
    private final DustyBotStoragePort dustyBotStoragePort = mock(DustyBotStoragePort.class);
    private final IndexerPort indexerPort = mock(IndexerPort.class);
    private final UUIDGeneratorPort uuidGeneratorPort = mock(UUIDGeneratorPort.class);
    private final GithubStoragePort githubStoragePort = mock(GithubStoragePort.class);
    private final ImageStoragePort imageStoragePort = mock(ImageStoragePort.class);
    private final ProjectObserverPort projectObserverPort = mock(ProjectObserverPort.class);
    private final ProjectService projectService = new ProjectService(projectObserverPort, projectStoragePort,
            imageStoragePort,
            uuidGeneratorPort, permissionService, indexerPort, dateProvider,
            contributionStoragePort, dustyBotStoragePort, githubStoragePort);

    @BeforeEach
    void setUp() {
        // Default expectations, should be overridden in test if needed
        when(permissionService.isUserProjectLead(any(ProjectId.class), any(UserId.class))).thenReturn(true);
    }

    @Test
    void should_create_project() {
        // Given
        final String imageUrl = faker.internet().image();
        final var usersToInviteAsProjectLeaders = List.of(faker.number().randomNumber());
        final List<UUID> ecosystemIds = List.of(UUID.randomUUID());
        final var categorySuggestions = List.of(faker.lorem().word(), faker.internet().slug());
        final CreateProjectCommand command = CreateProjectCommand.builder()
                .name(faker.pokemon().name())
                .shortDescription(faker.lorem().sentence())
                .longDescription(faker.lorem().paragraph())
                .isLookingForContributors(false)
                .moreInfos(List.of(NamedLink.builder().value(faker.lorem().sentence()).url(faker.internet().url()).build()))
                .firstProjectLeaderId(UserId.random())
                .githubUserIdsAsProjectLeadersToInvite(usersToInviteAsProjectLeaders)
                .githubRepoIds(List.of(faker.number().randomNumber()))
                .imageUrl(imageUrl)
                .ecosystemIds(ecosystemIds)
                .categorySuggestions(categorySuggestions)
                .build();
        final var expectedProjectId = ProjectId.random();
        final UserId projectLeadId = UserId.random();

        // When
        when(uuidGeneratorPort.generate()).thenReturn(expectedProjectId.value());
        final var projectIdentity = projectService.createProject(projectLeadId, command);

        // Then
        assertNotNull(projectIdentity);
        assertNotNull(projectIdentity.getLeft());
        assertThat(projectIdentity.getRight()).isEqualTo(Project.slugOf(command.getName()));
        verify(indexerPort, times(1)).indexUsers(usersToInviteAsProjectLeaders);
        final var projectIdArgumentCaptor = ArgumentCaptor.forClass(ProjectId.class);
        final var userIdArgumentCaptor = ArgumentCaptor.forClass(UserId.class);
        verify(projectObserverPort).onProjectCreated(projectIdArgumentCaptor.capture(), userIdArgumentCaptor.capture());
        verify(projectObserverPort).onLinkedReposChanged(expectedProjectId,
                command.getGithubRepoIds().stream().collect(Collectors.toUnmodifiableSet()), Set.of());
        verify(projectObserverPort).onProjectCategorySuggested(categorySuggestions.get(0), projectLeadId);
        verify(projectObserverPort).onProjectCategorySuggested(categorySuggestions.get(1), projectLeadId);
        assertEquals(expectedProjectId, projectIdArgumentCaptor.getAllValues().get(0));
        assertEquals(projectLeadId, userIdArgumentCaptor.getAllValues().get(0));
    }


    @Test
    void should_update_project() {
        // Given
        final String imageUrl = faker.internet().image();
        final var usersToInviteAsProjectLeaders = List.of(faker.number().randomNumber());
        final ProjectId projectId = ProjectId.random();
        final UserId projectLeadId = UserId.random();
        final var categorySuggestions = List.of(faker.lorem().word(), faker.internet().slug());
        final var contributorLabels = List.of(ProjectContributorLabel.of(projectId, faker.lorem().word()), ProjectContributorLabel.of(projectId,
                faker.internet().slug()));
        final UpdateProjectCommand command = UpdateProjectCommand.builder()
                .id(projectId)
                .name(faker.pokemon().name())
                .shortDescription(faker.lorem().sentence())
                .longDescription(faker.lorem().paragraph())
                .isLookingForContributors(false)
                .moreInfos(List.of(NamedLink.builder().value(faker.lorem().sentence()).url(faker.internet().url()).build()))
                .githubUserIdsAsProjectLeadersToInvite(usersToInviteAsProjectLeaders)
                .projectLeadersToKeep(List.of(projectLeadId))
                .githubRepoIds(List.of(faker.number().numberBetween(10L, 20L)))
                .imageUrl(imageUrl)
                .rewardSettings(
                        new ProjectRewardSettings(
                                true,
                                true,
                                false,
                                faker.date().birthday()
                        ))
                .categorySuggestions(categorySuggestions)
                .contributorLabels(contributorLabels)
                .build();

        // When
        when(permissionService.isUserProjectLead(projectId, projectLeadId)).thenReturn(true);
        when(projectStoragePort.getProjectLeadIds(projectId)).thenReturn(List.of(projectLeadId));
        when(projectStoragePort.getProjectRepoIds(projectId)).thenReturn(new HashSet<>(Arrays.asList(1L, 2L, 3L)));
        when(projectStoragePort.getProjectCategorySuggestions(projectId)).thenReturn(List.of(
                new ProjectCategorySuggestion(ProjectCategorySuggestion.Id.random(), categorySuggestions.get(0), projectId),
                new ProjectCategorySuggestion(ProjectCategorySuggestion.Id.random(), faker.lordOfTheRings().character(), projectId)));

        projectService.updateProject(projectLeadId, command);

        // Then
        verify(indexerPort, times(1)).indexUsers(usersToInviteAsProjectLeaders);
        verify(projectStoragePort, times(1)).updateProject(command.getId(),
                Project.slugOf(command.getName()),
                command.getName(),
                command.getShortDescription(),
                command.getLongDescription(), command.getIsLookingForContributors(),
                command.getMoreInfos(), command.getGithubRepoIds(),
                command.getGithubUserIdsAsProjectLeadersToInvite(),
                command.getProjectLeadersToKeep(),
                imageUrl,
                command.getRewardSettings(),
                command.getEcosystemIds(),
                command.getCategoryIds(),
                categorySuggestions,
                contributorLabels);
        verify(projectObserverPort).onLinkedReposChanged(projectId,
                command.getGithubRepoIds().stream().collect(Collectors.toUnmodifiableSet()), Set.of(1L, 2L, 3L));
        verify(projectObserverPort).onRewardSettingsChanged(projectId);
        verify(projectObserverPort, never()).onProjectCategorySuggested(categorySuggestions.get(0), projectLeadId);
        verify(projectObserverPort).onProjectCategorySuggested(categorySuggestions.get(1), projectLeadId);
    }

    @Test
    void should_not_update_project_when_user_is_not_lead() {
        // Given
        final String imageUrl = faker.internet().image();
        final var usersToInviteAsProjectLeaders = List.of(faker.number().randomNumber());
        final ProjectId projectId = ProjectId.random();
        final UserId projectLeadId = UserId.random();
        final UpdateProjectCommand command = UpdateProjectCommand.builder()
                .id(projectId)
                .name(faker.pokemon().name())
                .shortDescription(faker.lorem().sentence())
                .longDescription(faker.lorem().paragraph())
                .isLookingForContributors(false)
                .moreInfos(List.of(NamedLink.builder().value(faker.lorem().sentence()).url(faker.internet().url()).build()))
                .githubUserIdsAsProjectLeadersToInvite(usersToInviteAsProjectLeaders)
                .projectLeadersToKeep(List.of(UserId.random()))
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
        final InputStream imageInputStream = mock(InputStream.class);
        final String imageUrl = faker.internet().image();

        // When
        when(imageStoragePort.storeImage(imageInputStream)).thenReturn(new URL(imageUrl));
        final URL url = projectService.saveLogoImage(imageInputStream);

        // Then
        assertThat(url.toString()).isEqualTo(imageUrl);
    }

    @Test
    void should_check_project_lead_permissions_when_getting_project_rewardable_items_by_id_given_a_valid_project_lead() {
        final ProjectId projectId = ProjectId.random();
        final UserId projectLeadId = UserId.random();

        // When
        when(projectStoragePort.getProjectLeadIds(projectId))
                .thenReturn(List.of(UserId.random(), projectLeadId));
        projectService.getRewardableItemsPageByTypeForProjectLeadAndContributorId(projectId, null, null, projectLeadId,
                12345L, 0, 50, null, null);

        // Then
        verify(projectStoragePort, times(1)).getProjectRewardableItemsByTypeForProjectLeadAndContributorId(projectId,
                null, null, 12345L, 0, 50, null, null);
    }

    @Test
    void should_throw_forbidden_exception_when_getting_project_rewardable_items_by_id_given_an_invalid_project_lead() {
        final ProjectId projectId = ProjectId.random();
        final UserId projectLeadId = UserId.random();

        // When
        when(permissionService.isUserProjectLead(projectId, projectLeadId)).thenReturn(false);
        OnlyDustException onlyDustException = null;
        try {
            projectService.getRewardableItemsPageByTypeForProjectLeadAndContributorId(projectId, null, null,
                    projectLeadId, 1234L, 0, 50, null, null);
        } catch (OnlyDustException e) {
            onlyDustException = e;
        }

        // Then
        verify(projectStoragePort, times(0)).getProjectRewardableItemsByTypeForProjectLeadAndContributorId(any(),
                any(), any(), any(), anyInt(), anyInt(), any(), anyBoolean());
        assertNotNull(onlyDustException);
        assertEquals(403, onlyDustException.getStatus());
        assertEquals("Only project leads can read rewardable items on their projects", onlyDustException.getMessage());
    }

    @Test
    void should_check_project_lead_permissions_when_getting_completed_rewardable_items_given_a_valid_project_lead() {
        final ProjectId projectId = ProjectId.random();
        final UserId projectLeadId = UserId.random();

        // When
        when(projectStoragePort.getProjectLeadIds(projectId))
                .thenReturn(List.of(UserId.random(), projectLeadId));
        projectService.getAllCompletedRewardableItemsForProjectLeadAndContributorId(projectId, projectLeadId, 12345L);

        // Then
        verify(projectStoragePort, times(1)).getProjectRewardableItemsByTypeForProjectLeadAndContributorId(projectId,
                null, ContributionStatus.COMPLETED, 12345L, 0, 1_000_000, null, false);
    }

    @Test
    void should_throw_forbidden_exception_when_getting_completed_rewardable_items_given_an_invalid_project_lead() {
        final var projectId = ProjectId.random();
        final var projectLeadId = UserId.random();

        // When
        when(permissionService.isUserProjectLead(projectId, projectLeadId)).thenReturn(false);
        OnlyDustException onlyDustException = null;
        try {
            projectService.getAllCompletedRewardableItemsForProjectLeadAndContributorId(projectId, projectLeadId,
                    1234L);
        } catch (OnlyDustException e) {
            onlyDustException = e;
        }

        // Then
        verify(projectStoragePort, times(0)).getProjectRewardableItemsByTypeForProjectLeadAndContributorId(any(),
                any(), any(), any(), anyInt(), anyInt(), any(), anyBoolean());
        assertNotNull(onlyDustException);
        assertEquals(403, onlyDustException.getStatus());
        assertEquals("Only project leads can read rewardable items on their projects", onlyDustException.getMessage());
    }

    @Test
    void should_throw_forbidden_exception_when_creating_rewardable_issue_given_an_invalid_project_lead() {
        final ProjectId projectId = ProjectId.random();
        final UserId projectLeadId = UserId.random();

        // When
        when(permissionService.isUserProjectLead(projectId, projectLeadId)).thenReturn(false);
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
        final ProjectId projectId = ProjectId.random();
        final UserId projectLeadId = UserId.random();
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
        final ProjectId projectId = ProjectId.random();
        final UserId projectLeadId = UserId.random();
        final Long githubRepoId = 1L;
        final String githubRepoOwner = faker.name().username();
        final String githubRepoName = faker.pokemon().name();
        final String title = faker.pokemon().name();
        final String description = faker.lorem().paragraph();
        final Long issueNumber = 1234L;
        final GithubRepo repo =
                GithubRepo.builder().id(githubRepoId).owner(githubRepoOwner).name(githubRepoName).build();

        // When
        when(permissionService.isUserProjectLead(projectId, projectLeadId)).thenReturn(true);
        when(permissionService.isRepoLinkedToProject(projectId, githubRepoId)).thenReturn(true);
        when(githubStoragePort.findRepoById(githubRepoId))
                .thenReturn(Optional.of(GithubRepo.builder()
                        .id(githubRepoId)
                        .owner(githubRepoOwner)
                        .name(githubRepoName)
                        .build()));
        when(dustyBotStoragePort.createIssue(repo, title, description))
                .thenReturn(RewardableItemView.builder().number(issueNumber).build());
        when(dustyBotStoragePort.closeIssue(repo, issueNumber))
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
        verify(dustyBotStoragePort, times(1)).createIssue(repo, title, description);
        verify(dustyBotStoragePort, times(1)).closeIssue(repo, issueNumber);
    }

    @Test
    void should_add_rewardable_issue() {
        final ProjectId projectId = ProjectId.random();
        final UserId projectLeadId = UserId.random();
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
    void should_add_rewardable_issue_with_trailing_slash() {
        final ProjectStoragePort projectStoragePort = mock(ProjectStoragePort.class);
        final PermissionService permissionService = new PermissionService(projectStoragePort,
                mock(ContributionStoragePort.class), mock(SponsorStoragePort.class), mock(ProgramStoragePort.class), mock(EcosystemStoragePort.class));
        final IndexerPort indexerPort = mock(IndexerPort.class);
        final ProjectService projectService = new ProjectService(mock(ProjectObserverPort.class), projectStoragePort,
                mock(ImageStoragePort.class),
                mock(UUIDGeneratorPort.class), permissionService, indexerPort, dateProvider,
                mock(ContributionStoragePort.class), mock(DustyBotStoragePort.class), mock(GithubStoragePort.class));
        final ProjectId projectId = ProjectId.random();
        final UserId projectLeadId = UserId.random();
        final String githubRepoOwner = faker.name().username();
        final String githubRepoName = faker.pokemon().name();
        final Long issueNumber = 1234L;

        // When
        when(projectStoragePort.getProjectLeadIds(OrSlug.of(projectId)))
                .thenReturn(List.of(projectLeadId));
        when(projectStoragePort.getRewardableIssue(githubRepoOwner, githubRepoName, issueNumber))
                .thenReturn(RewardableItemView.builder().number(issueNumber).build());

        projectService.addRewardableIssue(projectId, projectLeadId, "https://github.com/%s/%s/issues/%d/".formatted(
                githubRepoOwner, githubRepoName, issueNumber));

        // Then
        verify(indexerPort, times(1)).indexIssue(githubRepoOwner, githubRepoName, issueNumber);
    }

    @Test
    void should_reject_invalid_issue_url() {
        final ProjectId projectId = ProjectId.random();
        final UserId projectLeadId = UserId.random();
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
        final ProjectId projectId = ProjectId.random();
        final UserId projectLeadId = UserId.random();
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
    void should_add_rewardable_pull_request_with_trailing_slash() {
        final ProjectStoragePort projectStoragePort = mock(ProjectStoragePort.class);
        final PermissionService permissionService = new PermissionService(projectStoragePort,
                mock(ContributionStoragePort.class), mock(SponsorStoragePort.class), mock(ProgramStoragePort.class), mock(EcosystemStoragePort.class));
        final IndexerPort indexerPort = mock(IndexerPort.class);
        final ProjectService projectService = new ProjectService(mock(ProjectObserverPort.class), projectStoragePort,
                mock(ImageStoragePort.class),
                mock(UUIDGeneratorPort.class), permissionService, indexerPort, dateProvider,
                mock(ContributionStoragePort.class), mock(DustyBotStoragePort.class), mock(GithubStoragePort.class));
        final ProjectId projectId = ProjectId.random();
        final UserId projectLeadId = UserId.random();
        final String githubRepoOwner = faker.name().username();
        final String githubRepoName = faker.pokemon().name();
        final Long pullRequestNumber = 1234L;

        // When
        when(projectStoragePort.getProjectLeadIds(OrSlug.of(projectId)))
                .thenReturn(List.of(projectLeadId));
        when(projectStoragePort.getRewardablePullRequest(githubRepoOwner, githubRepoName, pullRequestNumber))
                .thenReturn(RewardableItemView.builder().number(pullRequestNumber).build());

        projectService.addRewardablePullRequest(projectId, projectLeadId, "https://github.com/%s/%s/pull/%d/".formatted(
                githubRepoOwner, githubRepoName, pullRequestNumber));

        // Then
        verify(indexerPort, times(1)).indexPullRequest(githubRepoOwner, githubRepoName, pullRequestNumber);
    }

    @Test
    void should_reject_invalid_pull_request_url() {
        final ProjectId projectId = ProjectId.random();
        final UserId projectLeadId = UserId.random();
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

    @Test
    void should_forbid_access_to_contributions_for_non_leaders() {
        // Given
        final var projectId = ProjectId.random();
        final var projectLead = AuthenticatedUser.builder()
                .id(UserId.random())
                .githubUserId(faker.number().randomNumber())
                .build();

        // When
        when(permissionService.isUserProjectLead(projectId, projectLead.id())).thenReturn(false);

        // Then
        assertThatThrownBy(() -> projectService.contributions(projectId, projectLead, null, null, null, null, null))
                .isInstanceOf(OnlyDustException.class).hasMessage("Only project leads can list project contributions");
    }

    @Test
    void should_list_project_contributions() {
        // Given
        final var projectId = ProjectId.random();
        final var projectLead = AuthenticatedUser.builder()
                .id(UserId.random())
                .githubUserId(faker.number().randomNumber())
                .build();
        final var filters = ContributionView.Filters.builder()
                .contributors(List.of(faker.number().randomNumber()))
                .projects(List.of(projectId))
                .from(Date.from(Instant.now().minusSeconds(3600)))
                .from(Date.from(Instant.now()))
                .types(List.of(ContributionType.ISSUE))
                .statuses(List.of(ContributionStatus.COMPLETED))
                .repos(List.of(faker.number().randomNumber()))
                .build();

        final var sort = ContributionView.Sort.CONTRIBUTOR_LOGIN;
        final var direction = SortDirection.asc;
        final var page = 1;
        final var pageSize = 10;

        final var expectedContributions = Page.<ContributionView>builder()
                .content(List.of(ContributionView.builder()
                        .id(UUID.randomUUID().toString())
                        .build()))
                .totalItemNumber(1)
                .totalPageNumber(1)
                .build();

        when(contributionStoragePort.findContributions(Optional.of(projectLead.githubUserId()), filters, sort, direction, page,
                pageSize))
                .thenReturn(expectedContributions);

        // When
        final var contributions = projectService.contributions(projectId, projectLead, filters, sort, direction,
                page, pageSize);

        // Then
        assertEquals(expectedContributions, contributions);
    }

    @Test
    void should_hide_contributors() {
        // Given
        final var projectId = ProjectId.random();
        final var projectLeadId = UserId.random();
        final var contributorId = faker.number().randomNumber();
        when(permissionService.isUserProjectLead(projectId, projectLeadId)).thenReturn(true);

        // When
        projectService.hideContributorForProjectLead(projectId, projectLeadId, contributorId);

        // Then
        verify(projectStoragePort, times(1)).hideContributorForProjectLead(projectId, projectLeadId, contributorId);
    }

    @Test
    void should_show_contributors() {
        // Given
        final var projectId = ProjectId.random();
        final var projectLeadId = UserId.random();
        final var contributorId = faker.number().randomNumber();
        when(permissionService.isUserProjectLead(projectId, projectLeadId)).thenReturn(true);

        // When
        projectService.showContributorForProjectLead(projectId, projectLeadId, contributorId);

        // Then
        verify(projectStoragePort, times(1)).showContributorForProjectLead(projectId, projectLeadId, contributorId);
    }

    @Test
    void should_prevent_non_project_leads_from_hiding_contributors() {
        // Given
        final var projectId = ProjectId.random();
        final var projectLeadId = UserId.random();
        final var contributorId = faker.number().randomNumber();
        when(permissionService.isUserProjectLead(projectId, projectLeadId)).thenReturn(false);

        // When
        assertThatThrownBy(() -> projectService.hideContributorForProjectLead(projectId, projectLeadId, contributorId))
                // Then
                .isInstanceOf(OnlyDustException.class)
                .hasMessage("Only project leads can hide contributors on their projects");
    }

    @Test
    void should_prevent_non_project_leads_from_showing_contributors() {
        // Given
        final var projectId = ProjectId.random();
        final var projectLeadId = UserId.random();
        final var contributorId = faker.number().randomNumber();
        when(permissionService.isUserProjectLead(projectId, projectLeadId)).thenReturn(false);

        // When
        assertThatThrownBy(() -> projectService.showContributorForProjectLead(projectId, projectLeadId, contributorId))
                // Then
                .isInstanceOf(OnlyDustException.class)
                .hasMessage("Only project leads can show contributors on their projects");
    }
}
