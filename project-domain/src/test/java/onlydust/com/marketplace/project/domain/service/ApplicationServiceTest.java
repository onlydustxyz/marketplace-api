package onlydust.com.marketplace.project.domain.service;

import static onlydust.com.marketplace.kernel.exception.OnlyDustException.forbidden;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.github.javafaker.Faker;

import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import onlydust.com.marketplace.kernel.model.ProjectId;
import onlydust.com.marketplace.kernel.model.UserId;
import onlydust.com.marketplace.kernel.model.github.GithubUserIdentity;
import onlydust.com.marketplace.project.domain.model.*;
import onlydust.com.marketplace.project.domain.port.output.*;

public class ApplicationServiceTest {
    private final Faker faker = new Faker();
    private final UserStoragePort userStoragePort = mock(UserStoragePort.class);
    private final ProjectApplicationStoragePort projectApplicationStoragePort = mock(ProjectApplicationStoragePort.class);
    private final ProjectStoragePort projectStoragePort = mock(ProjectStoragePort.class);
    private final ApplicationObserverPort applicationObserver = mock(ApplicationObserverPort.class);
    private final GithubUserPermissionsService githubUserPermissionsService = mock(GithubUserPermissionsService.class);
    private final GithubStoragePort githubStoragePort = mock(GithubStoragePort.class);
    private final GithubApiPort githubApiPort = mock(GithubApiPort.class);
    private final GithubCommandService githubCommandService = mock(GithubCommandService.class);
    private final GithubAuthenticationPort githubAuthenticationPort = mock(GithubAuthenticationPort.class);
    private final GithubAppService githubAppService = mock(GithubAppService.class);

    private final ApplicationService applicationService = new ApplicationService(
            userStoragePort,
            projectApplicationStoragePort,
            projectStoragePort,
            applicationObserver,
            githubUserPermissionsService,
            githubStoragePort,
            githubApiPort,
            githubCommandService,
            githubAuthenticationPort,
            githubAppService);

    final Long githubUserId = faker.number().randomNumber(10, true);
    final UserId userId = UserId.random();
    final GithubIssue issue = new GithubIssue(GithubIssue.Id.random(),
            faker.number().randomNumber(10, true),
            faker.number().randomNumber(10, true),
            faker.rickAndMorty().character(),
            faker.rickAndMorty().quote(),
            faker.internet().url(),
            faker.rickAndMorty().character(),
            0, faker.pokemon().name(), faker.pokemon().name(), List.of());
    final GithubComment.Id commentId = GithubComment.Id.random();
    final String githubComment = faker.lorem().sentence();
    final String personalAccessToken = faker.internet().password();
    final ProjectId projectId = ProjectId.random();
    final Project project = Project.builder()
            .id(projectId)
            .slug(faker.lorem().word())
            .build();

    @BeforeEach
    void setUp() {
        reset(userStoragePort, projectStoragePort, applicationObserver, githubUserPermissionsService, githubStoragePort, githubApiPort,
                githubAppService, githubAuthenticationPort);

        when(githubUserPermissionsService.isUserAuthorizedToApplyOnProject(githubUserId)).thenReturn(true);
        when(githubStoragePort.findIssueById(issue.id())).thenReturn(Optional.of(issue));
        when(githubAuthenticationPort.getGithubPersonalToken(githubUserId)).thenReturn(personalAccessToken);
        when(projectApplicationStoragePort.findApplication(githubUserId, projectId, issue.id())).thenReturn(Optional.empty());
        when(projectStoragePort.getProjectRepoIds(projectId)).thenReturn(Set.of(faker.number().randomNumber(), issue.repoId()));
        when(projectStoragePort.getById(projectId)).thenReturn(Optional.of(project));
    }

    @Test
    void should_reject_application_if_user_is_not_allowed_to_comment_on_issues() {
        // Given
        when(githubUserPermissionsService.isUserAuthorizedToApplyOnProject(githubUserId)).thenReturn(false);

        // When
        assertThatThrownBy(() -> applicationService.applyOnProject(githubUserId, projectId, issue.id(), githubComment))
                // Then
                .isInstanceOf(OnlyDustException.class).hasMessage("User is not authorized to apply on project");
    }

    @Test
    void should_reject_application_if_issue_does_not_exists() {
        // Given
        when(githubStoragePort.findIssueById(issue.id())).thenReturn(Optional.empty());

        // When
        assertThatThrownBy(() -> applicationService.applyOnProject(githubUserId, projectId, issue.id(), githubComment))
                // Then
                .isInstanceOf(OnlyDustException.class).hasMessage("Issue %s not found".formatted(issue.id()));
    }

    @Test
    void should_reject_application_if_issue_is_already_assigned() {
        // Given
        when(githubStoragePort.findIssueById(issue.id())).thenReturn(Optional.of(new GithubIssue(GithubIssue.Id.random(),
                faker.number().randomNumber(10, true),
                faker.number().randomNumber(10, true),
                faker.rickAndMorty().character(),
                faker.rickAndMorty().quote(),
                faker.internet().url(),
                faker.rickAndMorty().character(),
                1, faker.pokemon().name(), faker.pokemon().name(), List.of())));

        // When
        assertThatThrownBy(() -> applicationService.applyOnProject(githubUserId, projectId, issue.id(), githubComment))
                // Then
                .isInstanceOf(OnlyDustException.class).hasMessage("Issue %s is already assigned".formatted(issue.id()));
    }

    @Test
    void should_reject_duplicate_applications() {
        // Given
        final var application = new Application(Application.Id.random(), projectId, githubUserId, Application.Origin.MARKETPLACE, ZonedDateTime.now(),
                issue.id(), commentId, githubComment);
        when(projectApplicationStoragePort.saveNew(application)).thenReturn(false);

        // When
        assertThatThrownBy(() -> applicationService.applyOnProject(githubUserId, projectId, issue.id(), githubComment))
                // Then
                .isInstanceOf(OnlyDustException.class).hasMessage("User %d already applied to issue %s".formatted(githubUserId, issue.id()));
    }

    @Test
    void should_reject_applications_if_repo_does_not_belong_to_project() {
        // Given
        when(projectStoragePort.getProjectRepoIds(projectId)).thenReturn(Set.of(faker.number().randomNumber()));
        when(projectApplicationStoragePort.saveNew(any())).thenReturn(true);

        // When
        assertThatThrownBy(() -> applicationService.applyOnProject(githubUserId, projectId, issue.id(), githubComment))
                // Then
                .isInstanceOf(OnlyDustException.class).hasMessage("Issue %s does not belong to project %s".formatted(issue.id(), projectId));
    }

    @Test
    void should_apply_on_project() {
        // When
        when(projectApplicationStoragePort.saveNew(any())).thenReturn(true);
        final var application = applicationService.applyOnProject(githubUserId, projectId, issue.id(), githubComment);

        // Then
        assertThat(application.id()).isNotNull();
        assertThat(application.projectId()).isEqualTo(projectId);
        assertThat(application.applicantId()).isEqualTo(githubUserId);
        assertThat(application.appliedAt()).isCloseTo(ZonedDateTime.now(), within(2, ChronoUnit.SECONDS));
        assertThat(application.origin()).isEqualTo(Application.Origin.MARKETPLACE);
        assertThat(application.issueId()).isEqualTo(issue.id());
        assertThat(application.commentId()).isNull();
        assertThat(application.commentBody()).isNull();

        verify(projectApplicationStoragePort).saveNew(application);
        verify(applicationObserver).onApplicationCreationStarted(application);
        verify(githubCommandService).createComment(application.id(), issue, githubUserId, githubComment);
    }

    @Test
    void should_prevent_delete_of_an_application_if_not_found() {
        // Given
        final var applicationId = Application.Id.random();

        when(projectApplicationStoragePort.findApplication(applicationId)).thenReturn(Optional.empty());

        // When
        assertThatThrownBy(() -> applicationService.deleteApplication(applicationId, userId, githubUserId, false))
                // Then
                .isInstanceOf(OnlyDustException.class)
                .hasMessage("Application %s not found".formatted(applicationId));
    }

    @Test
    void should_prevent_delete_of_another_contributor_application() {
        // Given
        final var application = Application.fromMarketplace(
                projectId,
                faker.number().randomNumber(),
                issue.id()
        );

        when(projectApplicationStoragePort.findApplication(application.id())).thenReturn(Optional.of(application));
        when(projectStoragePort.getProjectLeadIds(projectId)).thenReturn(List.of(UserId.random()));

        // When
        assertThatThrownBy(() -> applicationService.deleteApplication(application.id(), userId, githubUserId, false))
                // Then
                .isInstanceOf(OnlyDustException.class)
                .hasMessage("User is not authorized to delete this application");
    }

    @Test
    void should_delete_my_application() {
        // Given
        final var application = Application.fromMarketplace(
                projectId,
                githubUserId,
                issue.id()
        );

        when(projectApplicationStoragePort.findApplication(application.id())).thenReturn(Optional.of(application));

        // When
        applicationService.deleteApplication(application.id(), userId, githubUserId, true);

        // Then
        verify(projectApplicationStoragePort).deleteApplications(application.id());
        verify(applicationObserver).onApplicationDeleted(application);
        verify(githubApiPort).deleteComment(personalAccessToken, issue.repoId(), application.commentId());
    }

    @Test
    void should_delete_my_github_application() {
        // Given
        final var application = Application.fromGithubComment(new GithubComment(
                        GithubComment.Id.random(),
                        issue.id(),
                        issue.repoId(),
                        githubUserId,
                        ZonedDateTime.now().minusDays(1),
                        faker.lorem().sentence()
                ),
                projectId
        );

        when(projectApplicationStoragePort.findApplication(application.id())).thenReturn(Optional.of(application));

        // When
        applicationService.deleteApplication(application.id(), userId, githubUserId, false);

        // Then
        verify(projectApplicationStoragePort).deleteApplications(application.id());
        verify(applicationObserver).onApplicationDeleted(application);
        verifyNoInteractions(githubApiPort);
    }


    @Test
    void should_not_delete_github_comment_if_not_authorized() {
        // Given
        final var application = Application.fromMarketplace(
                projectId,
                githubUserId,
                issue.id()
        );

        doThrow(forbidden("User is not authorized to delete this application")).when(githubApiPort).deleteComment(any(), any(), any());
        when(projectApplicationStoragePort.findApplication(application.id())).thenReturn(Optional.of(application));

        // When
        assertThatThrownBy(() -> applicationService.deleteApplication(application.id(), userId, githubUserId, true))
                .isInstanceOf(OnlyDustException.class)
                .hasMessage("User is not authorized to delete this application");

        // Then
        verify(projectApplicationStoragePort).deleteApplications(application.id());
        verify(applicationObserver).onApplicationDeleted(application);
        verify(githubApiPort).deleteComment(personalAccessToken, issue.repoId(), application.commentId());
    }

    @Test
    void should_delete_application_as_project_lead() {
        // Given
        final var application = Application.fromMarketplace(
                projectId,
                faker.number().randomNumber(),
                issue.id()
        );

        when(projectApplicationStoragePort.findApplication(application.id())).thenReturn(Optional.of(application));
        when(projectStoragePort.getProjectLeadIds(projectId)).thenReturn(List.of(userId));

        // When
        applicationService.deleteApplication(application.id(), userId, githubUserId, true);

        // Then
        verify(projectApplicationStoragePort).deleteApplications(application.id());
        verify(applicationObserver).onApplicationDeleted(application);
        verifyNoInteractions(githubApiPort);
    }

    @Nested
    class AcceptApplication {
        final GithubUserIdentity applicant = GithubUserIdentity.builder()
                .githubUserId(githubUserId)
                .login(faker.name().username())
                .build();

        final Application application = Application.fromMarketplace(
                projectId,
                githubUserId,
                issue.id()
        );

        final GithubAppAccessToken githubToken = new GithubAppAccessToken(faker.internet().password(), Map.of("issues", "write"));

        @BeforeEach
        void setUp() {
            when(projectApplicationStoragePort.findApplication(application.id())).thenReturn(Optional.of(application));
            when(projectStoragePort.getProjectLeadIds(projectId)).thenReturn(List.of(userId));
            when(githubStoragePort.findIssueById(issue.id())).thenReturn(Optional.of(issue));
            when(userStoragePort.getIndexedUserByGithubId(application.applicantId())).thenReturn(Optional.of(applicant));
            when(githubAppService.getInstallationTokenFor(issue.repoId())).thenReturn(Optional.of(githubToken));
        }

        @Test
        void should_prevent_accepting_application_if_not_found() {
            // Given
            when(projectApplicationStoragePort.findApplication(application.id())).thenReturn(Optional.empty());

            // When
            assertThatThrownBy(() -> applicationService.acceptApplication(application.id(), userId))
                    // Then
                    .isInstanceOf(OnlyDustException.class)
                    .hasMessage("Application %s not found".formatted(application.id()));
        }

        @Test
        void should_prevent_accepting_application_if_not_project_lead() {
            // Given
            when(projectStoragePort.getProjectLeadIds(projectId)).thenReturn(List.of(UserId.random()));

            // When
            assertThatThrownBy(() -> applicationService.acceptApplication(application.id(), userId))
                    // Then
                    .isInstanceOf(OnlyDustException.class)
                    .hasMessage("User is not authorized to accept this application");
        }

        @Test
        void should_prevent_accepting_application_if_issue_not_found() {
            // Given
            when(githubStoragePort.findIssueById(issue.id())).thenReturn(Optional.empty());

            // When
            assertThatThrownBy(() -> applicationService.acceptApplication(application.id(), userId))
                    // Then
                    .isInstanceOf(OnlyDustException.class)
                    .hasMessage("Issue %s not found".formatted(issue.id()));
        }

        @Test
        void should_prevent_accepting_application_if_applicant_not_found() {
            // Given
            when(userStoragePort.getIndexedUserByGithubId(application.applicantId())).thenReturn(Optional.empty());

            // When
            assertThatThrownBy(() -> applicationService.acceptApplication(application.id(), userId))
                    // Then
                    .isInstanceOf(OnlyDustException.class)
                    .hasMessage("User %d not found".formatted(application.applicantId()));
        }

        @Test
        void should_prevent_accepting_application_if_github_app_not_installed() {
            // Given
            when(githubAppService.getInstallationTokenFor(issue.repoId())).thenReturn(Optional.empty());

            // When
            assertThatThrownBy(() -> applicationService.acceptApplication(application.id(), userId))
                    // Then
                    .isInstanceOf(OnlyDustException.class)
                    .hasMessage("Could not generate GitHub App token for repository %s".formatted(issue.repoId()));
        }

        @Test
        void should_accepting_application() {
            // When
            final Application refusedApplication = Application.fromMarketplace(
                    projectId,
                    githubUserId + 1,
                    issue.id()
            );
            when(projectApplicationStoragePort.findApplicationsOnIssueAndProject(application.issueId(), application.projectId()))
                    .thenReturn(List.of(application, refusedApplication));
            applicationService.acceptApplication(application.id(), userId);

            // Then
            verify(githubApiPort).assign(githubToken.token(), issue.repoId(), issue.number(), applicant.login());
            verify(applicationObserver).onApplicationAccepted(application, userId);
            verify(applicationObserver).onApplicationRefused(refusedApplication);
        }
    }

    @Nested
    class UpdateProjectApplication {
        final Application application = Application.fromMarketplace(
                projectId,
                githubUserId,
                issue.id()
        );

        @BeforeEach
        void setup() {
            when(projectApplicationStoragePort.findApplication(application.id())).thenReturn(Optional.of(application));
            when(projectStoragePort.getProjectLeadIds(projectId)).thenReturn(List.of(userId));
        }

        @Test
        void should_ignore_application() {
            // When
            applicationService.updateApplication(userId, application.id(), true);

            // Then
            final var applicationCaptor = ArgumentCaptor.forClass(Application.class);
            verify(projectApplicationStoragePort).save(applicationCaptor.capture());
            final var savedApplication = applicationCaptor.getValue();
            assertThat(savedApplication.ignoredAt()).isNotNull();
        }

        @Test
        void should_unignore_application() {
            // Given
            application.ignore();

            // When
            applicationService.updateApplication(userId, application.id(), false);

            // Then
            final var applicationCaptor = ArgumentCaptor.forClass(Application.class);
            verify(projectApplicationStoragePort).save(applicationCaptor.capture());
            final var savedApplication = applicationCaptor.getValue();
            assertThat(savedApplication.ignoredAt()).isNull();
        }

        @Test
        void should_not_update_application() {
            {
                // Given
                application.ignore();

                // When
                applicationService.updateApplication(userId, application.id(), null);

                // Then
                final var applicationCaptor = ArgumentCaptor.forClass(Application.class);
                verify(projectApplicationStoragePort).save(applicationCaptor.capture());
                final var savedApplication = applicationCaptor.getValue();
                assertThat(savedApplication.ignoredAt()).isNotNull();
            }

            clearInvocations(projectApplicationStoragePort);

            {
                // Given
                application.unIgnore();

                // When
                applicationService.updateApplication(userId, application.id(), null);

                // Then
                final var applicationCaptor = ArgumentCaptor.forClass(Application.class);
                verify(projectApplicationStoragePort).save(applicationCaptor.capture());
                final var savedApplication = applicationCaptor.getValue();
                assertThat(savedApplication.ignoredAt()).isNull();
            }
        }

        @Test
        void should_prevent_to_update_non_existing_application() {
            // Given
            when(projectApplicationStoragePort.findApplication(application.id())).thenReturn(Optional.empty());

            // When
            assertThatThrownBy(() ->
                    applicationService.updateApplication(userId, application.id(), true))
                    // Then
                    .isInstanceOf(OnlyDustException.class)
                    .hasMessage("Application %s not found".formatted(application.id()));
        }

        @Test
        void should_prevent_non_project_lead_to_update_application() {
            // Given
            when(projectStoragePort.getProjectLeadIds(projectId)).thenReturn(List.of(UserId.random()));

            // When
            assertThatThrownBy(() ->
                    applicationService.updateApplication(userId, application.id(), true))
                    // Then
                    .isInstanceOf(OnlyDustException.class)
                    .hasMessage("User is not authorized to update this application");
        }
    }
}
