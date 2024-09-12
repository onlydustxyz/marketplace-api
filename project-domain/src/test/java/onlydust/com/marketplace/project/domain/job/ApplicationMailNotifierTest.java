package onlydust.com.marketplace.project.domain.job;

import com.github.javafaker.Faker;
import onlydust.com.marketplace.kernel.model.ProjectId;
import onlydust.com.marketplace.kernel.model.UserId;
import onlydust.com.marketplace.kernel.model.github.GithubUserIdentity;
import onlydust.com.marketplace.kernel.port.output.NotificationPort;
import onlydust.com.marketplace.project.domain.model.Application;
import onlydust.com.marketplace.project.domain.model.GithubComment;
import onlydust.com.marketplace.project.domain.model.GithubIssue;
import onlydust.com.marketplace.project.domain.model.Project;
import onlydust.com.marketplace.project.domain.model.notification.ApplicationToReview;
import onlydust.com.marketplace.project.domain.port.output.GithubStoragePort;
import onlydust.com.marketplace.project.domain.port.output.ProjectStoragePort;
import onlydust.com.marketplace.project.domain.port.output.UserStoragePort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class ApplicationMailNotifierTest {
    final ProjectStoragePort projectStoragePort = mock(ProjectStoragePort.class);
    final GithubStoragePort githubStoragePort = mock(GithubStoragePort.class);
    final UserStoragePort userStoragePort = mock(UserStoragePort.class);
    final NotificationPort notificationPort = mock(NotificationPort.class);
    final ApplicationMailNotifier notifier = new ApplicationMailNotifier(projectStoragePort, githubStoragePort, userStoragePort, notificationPort);

    final Faker faker = new Faker();

    final Project project = Project.builder()
            .id(ProjectId.random())
            .name(faker.rickAndMorty().character())
            .slug(faker.internet().slug())
            .build();

    final GithubIssue issue = new GithubIssue(
            GithubIssue.Id.random(),
            faker.random().nextLong(),
            faker.random().nextLong(),
            faker.lorem().sentence(),
            faker.lorem().paragraph(),
            faker.internet().url(),
            faker.lordOfTheRings().character(),
            faker.number().randomDigit(),
            faker.rickAndMorty().character(),
            faker.rickAndMorty().location(),
            List.of()
    );

    @BeforeEach
    void setUp() {
        when(projectStoragePort.getById(project.getId())).thenReturn(Optional.of(project));
        when(githubStoragePort.findIssueById(issue.id())).thenReturn(Optional.of(issue));
    }

    @Test
    void on_application_created() {
        // Given
        final var application = new Application(
                Application.Id.random(),
                project.getId(),
                faker.random().nextLong(),
                Application.Origin.GITHUB,
                ZonedDateTime.now(),
                issue.id(),
                GithubComment.Id.random(),
                faker.lorem().sentence(),
                null
        );

        final var projectLeads = List.of(UserId.random(), UserId.random());
        when(projectStoragePort.getProjectLeadIds(project.getId())).thenReturn(projectLeads);
        when(userStoragePort.getRegisteredUserByGithubId(application.applicantId()))
                .thenReturn(Optional.empty());
        when(userStoragePort.getIndexedUserByGithubId(application.applicantId()))
                .thenReturn(Optional.of(GithubUserIdentity.builder()
                        .githubUserId(application.applicantId())
                        .login(faker.gameOfThrones().character())
                        .build()
                ));

        // When
        notifier.onApplicationCreated(application);

        // Then
        final var recipientCaptor = ArgumentCaptor.forClass(UserId.class);
        final var notificationCaptor = ArgumentCaptor.forClass(ApplicationToReview.class);
        verify(notificationPort, times(2)).push(recipientCaptor.capture(), notificationCaptor.capture());
        final var recipients = recipientCaptor.getAllValues();
        final var notifications = notificationCaptor.getAllValues();
        assertThat(recipients).containsExactlyElementsOf(projectLeads);
        assertThat(notifications).hasSize(2);
        assertThat(notifications).allMatch(notification -> notification.getProject().id().equals(project.getId()));
        assertThat(notifications).allMatch(notification -> notification.getProject().name().equals(project.getName()));
        assertThat(notifications).allMatch(notification -> notification.getProject().slug().equals(project.getSlug()));
        assertThat(notifications).allMatch(notification -> notification.getIssue().id().equals(issue.id().value()));
        assertThat(notifications).allMatch(notification -> notification.getIssue().title().equals(issue.title()));
        assertThat(notifications).allMatch(notification -> notification.getIssue().htmlUrl().equals(issue.htmlUrl()));
        assertThat(notifications).allMatch(notification -> notification.getIssue().repoName().equals(issue.repoName()));
        assertThat(notifications).allMatch(notification -> notification.getIssue().description().equals(issue.description()));
    }
}