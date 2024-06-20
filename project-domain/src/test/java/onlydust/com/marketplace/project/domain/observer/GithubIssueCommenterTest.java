package onlydust.com.marketplace.project.domain.observer;

import com.github.javafaker.Faker;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import onlydust.com.marketplace.project.domain.model.*;
import onlydust.com.marketplace.project.domain.port.output.GithubApiPort;
import onlydust.com.marketplace.project.domain.port.output.GithubStoragePort;
import onlydust.com.marketplace.project.domain.port.output.ProjectStoragePort;
import onlydust.com.marketplace.project.domain.port.output.UserStoragePort;
import onlydust.com.marketplace.project.domain.service.GithubAppService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.ZoneOffset;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class GithubIssueCommenterTest {
    final UserStoragePort userStoragePort = mock(UserStoragePort.class);
    final ProjectStoragePort projectStoragePort = mock(ProjectStoragePort.class);
    final GithubStoragePort githubStoragePort = mock(GithubStoragePort.class);
    final GithubAppService githubAppService = mock(GithubAppService.class);
    final GithubApiPort githubApiPort = mock(GithubApiPort.class);
    final GlobalConfig globalConfig = new GlobalConfig().setAppBaseUrl("https://local-app.onlydust.com");
    final GithubIssueCommenter githubIssueCommenter = new GithubIssueCommenter(
            userStoragePort,
            projectStoragePort,
            githubStoragePort,
            githubAppService,
            githubApiPort,
            globalConfig);

    final Faker faker = new Faker();

    @BeforeEach
    void setup() {
        reset(userStoragePort,
                projectStoragePort,
                githubStoragePort,
                githubAppService,
                githubApiPort);
    }

    @Nested
    class GivenAnInternalApplication {

        private final Application application = new Application(
                Application.Id.random(),
                UUID.randomUUID(),
                faker.number().randomNumber(10, true),
                Application.Origin.MARKETPLACE,
                faker.date().birthday().toInstant().atZone(ZoneOffset.UTC),
                GithubIssue.Id.random(),
                GithubComment.Id.random(),
                faker.lorem().paragraph(),
                faker.lorem().paragraph()
        );

        @Nested
        class OnApplicationCreated {
            @Test
            void should_do_nothing() {
                // When
                githubIssueCommenter.onApplicationCreated(application);

                // Then
                verifyNoInteractions(
                        userStoragePort,
                        projectStoragePort,
                        githubStoragePort,
                        githubAppService,
                        githubApiPort);
            }
        }
    }

    @Nested
    class GivenAnExternalApplication {
        final GithubIssue issue = new GithubIssue(
                GithubIssue.Id.random(),
                faker.number().randomNumber(),
                faker.number().randomNumber(),
                0);

        final Project project = Project.builder()
                .id(UUID.randomUUID())
                .name(faker.lorem().word())
                .slug(faker.internet().slug())
                .build();

        final User applicant = User.builder()
                .githubUserId(faker.number().randomNumber(10, true))
                .githubLogin(faker.internet().slug())
                .build();

        final Application application = new Application(
                Application.Id.random(),
                project.getId(),
                applicant.getGithubUserId(),
                Application.Origin.GITHUB,
                faker.date().birthday().toInstant().atZone(ZoneOffset.UTC),
                issue.id(),
                GithubComment.Id.random(),
                faker.lorem().paragraph(),
                null
        );

        private final GithubAppAccessToken githubAppToken = new GithubAppAccessToken(faker.lorem().word(), Map.of("issues", "write"));

        @BeforeEach
        void setup() {
            when(githubStoragePort.findIssueById(issue.id())).thenReturn(Optional.of(issue));
            when(githubAppService.getInstallationTokenFor(issue.repoId())).thenReturn(Optional.of(githubAppToken));
            when(projectStoragePort.getById(application.projectId())).thenReturn(Optional.of(project));
            when(userStoragePort.getUserByGithubId(applicant.getGithubUserId())).thenReturn(Optional.of(applicant));
        }

        @Test
        void should_throw_if_project_is_not_found() {
            // Given
            when(projectStoragePort.getById(any())).thenReturn(Optional.empty());

            // When
            assertThatThrownBy(() -> githubIssueCommenter.onApplicationCreated(application))
                    .isInstanceOf(OnlyDustException.class)
                    .hasMessage("Project %s not found".formatted(application.projectId()));

            // Then
            verifyNoInteractions(githubApiPort);
        }

        @Test
        void should_throw_if_applicant_is_not_found() {
            // Given
            when(userStoragePort.getUserByGithubId(any())).thenReturn(Optional.empty());

            // When
            assertThatThrownBy(() -> githubIssueCommenter.onApplicationCreated(application))
                    .isInstanceOf(OnlyDustException.class)
                    .hasMessage("User %s not found".formatted(application.applicantId()));

            // Then
            verifyNoInteractions(githubApiPort);
        }

        @Test
        void should_throw_if_issue_is_not_indexed() {
            // Given
            when(githubStoragePort.findIssueById(any())).thenReturn(Optional.empty());

            // When
            assertThatThrownBy(() -> githubIssueCommenter.onApplicationCreated(application))
                    .isInstanceOf(OnlyDustException.class)
                    .hasMessage("Issue %s not found".formatted(application.issueId()));

            // Then
            verifyNoInteractions(githubApiPort);
        }

        @Test
        void should_not_comment_issue_without_github_app_token() {
            // Given
            when(githubAppService.getInstallationTokenFor(issue.repoId())).thenReturn(Optional.empty());

            // When
            githubIssueCommenter.onApplicationCreated(application);

            // Then
            verifyNoInteractions(githubApiPort);
        }

        @Test
        void should_not_comment_issue_without_proper_permissions() {
            // Given
            when(githubAppService.getInstallationTokenFor(issue.repoId())).thenReturn(Optional.of(new GithubAppAccessToken(faker.lorem().word(), Map.of(
                    "issues", "read"))));

            // When
            githubIssueCommenter.onApplicationCreated(application);

            // Then
            verifyNoInteractions(githubApiPort);
        }

        @Test
        void should_create_application() {
            // When
            githubIssueCommenter.onApplicationCreated(application);

            // Then
            verify(githubApiPort).createComment(githubAppToken.token(), issue, """
                    Hey @%s!
                    Thanks for showing interest.
                    We've created an application for you to contribute to %s.
                    Go check it out on [OnlyDust](%s/p/%s)!
                    """.formatted(applicant.getGithubLogin(), project.getName(), globalConfig.getAppBaseUrl(), project.getSlug()));
        }
    }
}