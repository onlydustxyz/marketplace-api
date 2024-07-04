package onlydust.com.marketplace.project.domain.service;

import com.github.javafaker.Faker;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import onlydust.com.marketplace.kernel.model.github.GithubUserIdentity;
import onlydust.com.marketplace.project.domain.model.*;
import onlydust.com.marketplace.project.domain.port.output.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static onlydust.com.marketplace.kernel.exception.OnlyDustException.forbidden;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

public class ApplicationServiceTest {
    private final Faker faker = new Faker();
    private final UserStoragePort userStoragePort = mock(UserStoragePort.class);
    private final ProjectStoragePort projectStoragePort = mock(ProjectStoragePort.class);
    private final ApplicationObserverPort applicationObserver = mock(ApplicationObserverPort.class);
    private final GithubUserPermissionsService githubUserPermissionsService = mock(GithubUserPermissionsService.class);
    private final GithubStoragePort githubStoragePort = mock(GithubStoragePort.class);
    private final GithubApiPort githubApiPort = mock(GithubApiPort.class);
    private final GithubAuthenticationPort githubAuthenticationPort = mock(GithubAuthenticationPort.class);
    private final GithubAppService githubAppService = mock(GithubAppService.class);
    private final GlobalConfig globalConfig = new GlobalConfig().setAppBaseUrl("https://local-app.onlydust.xyz");

    private final ApplicationService applicationService = new ApplicationService(
            userStoragePort,
            projectStoragePort,
            applicationObserver,
            githubUserPermissionsService,
            githubStoragePort,
            githubApiPort,
            githubAuthenticationPort,
            githubAppService,
            globalConfig);

    final Long githubUserId = faker.number().randomNumber(10, true);
    final UUID userId = UUID.randomUUID();
    final GithubIssue issue = new GithubIssue(GithubIssue.Id.random(), faker.number().randomNumber(10, true), faker.number().randomNumber(10, true), 0);
    final GithubComment.Id commentId = GithubComment.Id.random();
    final String motivation = faker.lorem().sentence();
    final String problemSolvingApproach = faker.lorem().sentence();
    final String personalAccessToken = faker.internet().password();
    final UUID projectId = UUID.randomUUID();
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
        when(userStoragePort.findApplication(githubUserId, projectId, issue.id())).thenReturn(Optional.empty());
        when(projectStoragePort.getProjectRepoIds(projectId)).thenReturn(Set.of(faker.number().randomNumber(), issue.repoId()));
        when(projectStoragePort.getById(projectId)).thenReturn(Optional.of(project));
    }

    @Test
    void should_reject_application_if_user_is_not_allowed_to_comment_on_issues() {
        // Given
        when(githubUserPermissionsService.isUserAuthorizedToApplyOnProject(githubUserId)).thenReturn(false);

        // When
        assertThatThrownBy(() -> applicationService.applyOnProject(githubUserId, projectId, issue.id(), motivation, problemSolvingApproach))
                // Then
                .isInstanceOf(OnlyDustException.class).hasMessage("User is not authorized to apply on project");
    }

    @Test
    void should_reject_application_if_issue_does_not_exists() {
        // Given
        when(githubStoragePort.findIssueById(issue.id())).thenReturn(Optional.empty());

        // When
        assertThatThrownBy(() -> applicationService.applyOnProject(githubUserId, projectId, issue.id(), motivation, problemSolvingApproach))
                // Then
                .isInstanceOf(OnlyDustException.class).hasMessage("Issue %s not found".formatted(issue.id()));
    }

    @Test
    void should_reject_application_if_issue_is_already_assigned() {
        // Given
        when(githubStoragePort.findIssueById(issue.id())).thenReturn(Optional.of(new GithubIssue(issue.id(), faker.number().randomNumber(),
                faker.number().randomNumber(), 1)));

        // When
        assertThatThrownBy(() -> applicationService.applyOnProject(githubUserId, projectId, issue.id(), motivation, problemSolvingApproach))
                // Then
                .isInstanceOf(OnlyDustException.class).hasMessage("Issue %s is already assigned".formatted(issue.id()));
    }

    @Test
    void should_reject_duplicate_applications() {
        // Given
        final var application = new Application(Application.Id.random(), projectId, githubUserId, Application.Origin.MARKETPLACE, ZonedDateTime.now(),
                issue.id(), commentId, motivation, problemSolvingApproach);
        when(userStoragePort.findApplication(githubUserId, projectId, issue.id())).thenReturn(Optional.of(application));

        // When
        assertThatThrownBy(() -> applicationService.applyOnProject(githubUserId, projectId, issue.id(), motivation, problemSolvingApproach))
                // Then
                .isInstanceOf(OnlyDustException.class).hasMessage("User already applied to this issue");
    }

    @Test
    void should_reject_applications_if_project_does_not_exists() {
        // Given
        when(projectStoragePort.getById(projectId)).thenReturn(Optional.empty());

        // When
        assertThatThrownBy(() -> applicationService.applyOnProject(githubUserId, projectId, issue.id(), motivation, problemSolvingApproach))
                // Then
                .isInstanceOf(OnlyDustException.class).hasMessage("Project %s not found".formatted(projectId));
    }

    @Test
    void should_reject_applications_if_repo_does_not_belong_to_project() {
        // Given
        when(projectStoragePort.getProjectRepoIds(projectId)).thenReturn(Set.of(faker.number().randomNumber()));

        // When
        assertThatThrownBy(() -> applicationService.applyOnProject(githubUserId, projectId, issue.id(), motivation, problemSolvingApproach))
                // Then
                .isInstanceOf(OnlyDustException.class).hasMessage("Issue %s does not belong to project %s".formatted(issue.id(), projectId));
    }

    @Test
    void should_apply_on_project() {
        // Given
        when(githubApiPort.createComment(eq(personalAccessToken), eq(issue), any())).thenReturn(commentId);

        // When
        final var application = applicationService.applyOnProject(githubUserId, projectId, issue.id(), motivation, problemSolvingApproach);

        // Then
        assertThat(application.id()).isNotNull();
        assertThat(application.projectId()).isEqualTo(projectId);
        assertThat(application.applicantId()).isEqualTo(githubUserId);
        assertThat(application.appliedAt()).isEqualToIgnoringSeconds(ZonedDateTime.now());
        assertThat(application.origin()).isEqualTo(Application.Origin.MARKETPLACE);
        assertThat(application.issueId()).isEqualTo(issue.id());
        assertThat(application.commentId()).isEqualTo(commentId);
        assertThat(application.motivations()).isEqualTo(motivation);
        assertThat(application.problemSolvingApproach()).isEqualTo(problemSolvingApproach);

        verify(userStoragePort).save(application);
        verify(applicationObserver).onApplicationCreated(application);
        verify(githubApiPort).createComment(personalAccessToken, issue, """
                I am applying to this issue via [OnlyDust platform](https://local-app.onlydust.xyz/p/%s).

                ### My background and how it can be leveraged
                %s

                ### How I plan on tackling this issue
                %s
                """.formatted(project.getSlug(), motivation, problemSolvingApproach));
    }

    @Test
    void should_apply_on_project_without_problem_solving_approach() {
        // Given
        when(githubApiPort.createComment(eq(personalAccessToken), eq(issue), any())).thenReturn(commentId);

        // When
        final var application = applicationService.applyOnProject(githubUserId, projectId, issue.id(), motivation, " ");

        // Then
        assertThat(application.problemSolvingApproach()).isEqualTo(" ");

        verify(githubApiPort).createComment(personalAccessToken, issue, """
                I am applying to this issue via [OnlyDust platform](https://local-app.onlydust.xyz/p/%s).

                ### My background and how it can be leveraged
                %s
                """.formatted(project.getSlug(), motivation));
    }

    @Test
    void should_prevent_update_of_non_existing_application() {
        // Given
        final var applicationId = Application.Id.random();

        when(userStoragePort.findApplication(applicationId)).thenReturn(Optional.empty());

        // When
        assertThatThrownBy(() -> applicationService.updateApplication(applicationId, githubUserId, faker.lorem().sentence(), faker.lorem().sentence()))
                // Then
                .isInstanceOf(OnlyDustException.class).hasMessage("Application %s not found".formatted(applicationId));
    }

    @Test
    void should_prevent_update_of_another_contributor_application() {
        // Given
        final var application = Application.fromMarketplace(
                projectId,
                faker.number().randomNumber(),
                issue.id(),
                GithubComment.Id.random(),
                faker.lorem().sentence(),
                faker.lorem().sentence()
        );

        when(userStoragePort.findApplication(application.id())).thenReturn(Optional.of(application));

        // When
        final var newMotivation = faker.lorem().sentence();
        final var newProblemSolvingApproach = faker.lorem().sentence();
        assertThatThrownBy(() -> applicationService.updateApplication(application.id(), githubUserId, newMotivation, newProblemSolvingApproach))
                // Then
                .isInstanceOf(OnlyDustException.class).hasMessage("User is not authorized to update this application");
    }

    @Test
    void should_prevent_application_update_if_no_github_permissions() {
        // Given
        final var application = Application.fromGithubComment(
                new GithubComment(GithubComment.Id.random(), issue.id(),
                        faker.number().randomNumber(),
                        githubUserId,
                        faker.date().past(3, TimeUnit.DAYS).toInstant().atZone(ZoneOffset.UTC),
                        faker.lorem().sentence()),
                projectId
        );

        when(userStoragePort.findApplication(application.id())).thenReturn(Optional.of(application));
        when(githubUserPermissionsService.isUserAuthorizedToApplyOnProject(githubUserId)).thenReturn(false);

        // When
        assertThatThrownBy(() -> applicationService.updateApplication(application.id(), githubUserId, motivation, problemSolvingApproach))
                // Then
                .isInstanceOf(OnlyDustException.class)
                .hasMessage("User is not authorized to update this application");
    }

    @Test
    void should_update_application() {
        // Given
        final var application = Application.fromGithubComment(
                new GithubComment(commentId,
                        issue.id(),
                        faker.number().randomNumber(),
                        githubUserId,
                        faker.date().past(3, TimeUnit.DAYS).toInstant().atZone(ZoneOffset.UTC),
                        faker.lorem().sentence()),
                projectId
        );

        when(userStoragePort.findApplication(application.id())).thenReturn(Optional.of(application));

        // When
        final var motivations = faker.lorem().sentence();
        final var problemSolvingApproach = faker.lorem().sentence();
        final var updatedApplication = applicationService.updateApplication(application.id(), githubUserId, motivations, problemSolvingApproach);

        // Then
        assertThat(updatedApplication.id()).isEqualTo(application.id());
        assertThat(updatedApplication.projectId()).isEqualTo(application.projectId());
        assertThat(updatedApplication.applicantId()).isEqualTo(application.applicantId());
        assertThat(updatedApplication.appliedAt()).isEqualTo(application.appliedAt());
        assertThat(updatedApplication.origin()).isEqualTo(Application.Origin.MARKETPLACE);
        assertThat(updatedApplication.motivations()).isEqualTo(motivations);
        assertThat(updatedApplication.problemSolvingApproach()).isEqualTo(problemSolvingApproach);
        verify(userStoragePort).save(updatedApplication);

        verify(githubApiPort).updateComment(personalAccessToken, issue.repoId(), commentId, """
                I am applying to this issue via [OnlyDust platform](https://local-app.onlydust.xyz/p/%s).

                ### My background and how it can be leveraged
                %s

                ### How I plan on tackling this issue
                %s
                """.formatted(project.getSlug(), updatedApplication.motivations(), updatedApplication.problemSolvingApproach()));
    }

    @Test
    void should_prevent_delete_of_an_application_if_not_found() {
        // Given
        final var applicationId = Application.Id.random();

        when(userStoragePort.findApplication(applicationId)).thenReturn(Optional.empty());

        // When
        assertThatThrownBy(() -> applicationService.deleteApplication(applicationId, userId, githubUserId))
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
                issue.id(),
                GithubComment.Id.random(),
                faker.lorem().sentence(),
                faker.lorem().sentence()
        );

        when(userStoragePort.findApplication(application.id())).thenReturn(Optional.of(application));
        when(projectStoragePort.getProjectLeadIds(projectId)).thenReturn(List.of(UUID.randomUUID()));

        // When
        assertThatThrownBy(() -> applicationService.deleteApplication(application.id(), userId, githubUserId))
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
                issue.id(),
                GithubComment.Id.random(),
                faker.lorem().sentence(),
                faker.lorem().sentence()
        );

        when(userStoragePort.findApplication(application.id())).thenReturn(Optional.of(application));

        // When
        applicationService.deleteApplication(application.id(), userId, githubUserId);

        // Then
        verify(userStoragePort).deleteApplications(application.id());
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

        when(userStoragePort.findApplication(application.id())).thenReturn(Optional.of(application));

        // When
        applicationService.deleteApplication(application.id(), userId, githubUserId);

        // Then
        verify(userStoragePort).deleteApplications(application.id());
        verifyNoInteractions(githubApiPort);
    }


    @Test
    void should_not_delete_github_comment_if_not_authorized() {
        // Given
        final var application = Application.fromMarketplace(
                projectId,
                githubUserId,
                issue.id(),
                GithubComment.Id.random(),
                faker.lorem().sentence(),
                faker.lorem().sentence()
        );

        doThrow(forbidden("User is not authorized to delete this application")).when(githubApiPort).deleteComment(any(), any(), any());
        when(userStoragePort.findApplication(application.id())).thenReturn(Optional.of(application));

        // When
        applicationService.deleteApplication(application.id(), userId, githubUserId);

        // Then
        verify(userStoragePort).deleteApplications(application.id());
        verify(githubApiPort).deleteComment(personalAccessToken, issue.repoId(), application.commentId());
    }

    @Test
    void should_delete_application_as_project_lead() {
        // Given
        final var application = Application.fromMarketplace(
                projectId,
                faker.number().randomNumber(),
                issue.id(),
                GithubComment.Id.random(),
                faker.lorem().sentence(),
                faker.lorem().sentence()
        );

        when(userStoragePort.findApplication(application.id())).thenReturn(Optional.of(application));
        when(projectStoragePort.getProjectLeadIds(projectId)).thenReturn(List.of(userId));

        // When
        applicationService.deleteApplication(application.id(), userId, githubUserId);

        // Then
        verify(userStoragePort).deleteApplications(application.id());
        verifyNoInteractions(githubApiPort);
    }

    @Nested
    class AcceptApplication {
        final GithubUserIdentity applicant = GithubUserIdentity.builder()
                .githubUserId(githubUserId)
                .githubLogin(faker.name().username())
                .build();

        final Application application = Application.fromMarketplace(
                projectId,
                githubUserId,
                issue.id(),
                GithubComment.Id.random(),
                faker.lorem().sentence(),
                faker.lorem().sentence()
        );

        final GithubAppAccessToken githubToken = new GithubAppAccessToken(faker.internet().password(), Map.of("issues", "write"));

        @BeforeEach
        void setUp() {
            when(userStoragePort.findApplication(application.id())).thenReturn(Optional.of(application));
            when(projectStoragePort.getProjectLeadIds(projectId)).thenReturn(List.of(userId));
            when(githubStoragePort.findIssueById(issue.id())).thenReturn(Optional.of(issue));
            when(userStoragePort.getIndexedUserByGithubId(application.applicantId())).thenReturn(Optional.of(applicant));
            when(githubAppService.getInstallationTokenFor(issue.repoId())).thenReturn(Optional.of(githubToken));
        }

        @Test
        void should_prevent_accepting_application_if_not_found() {
            // Given
            when(userStoragePort.findApplication(application.id())).thenReturn(Optional.empty());

            // When
            assertThatThrownBy(() -> applicationService.acceptApplication(application.id(), userId))
                    // Then
                    .isInstanceOf(OnlyDustException.class)
                    .hasMessage("Application %s not found".formatted(application.id()));
        }

        @Test
        void should_prevent_accepting_application_if_not_project_lead() {
            // Given
            when(projectStoragePort.getProjectLeadIds(projectId)).thenReturn(List.of(UUID.randomUUID()));

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
            applicationService.acceptApplication(application.id(), userId);

            // Then
            verify(githubApiPort).assign(githubToken.token(), issue.repoId(), issue.number(), applicant.getGithubLogin());
        }
    }
}
