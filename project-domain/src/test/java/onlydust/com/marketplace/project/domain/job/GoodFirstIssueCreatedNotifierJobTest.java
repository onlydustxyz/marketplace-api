package onlydust.com.marketplace.project.domain.job;

import com.github.javafaker.Faker;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import onlydust.com.marketplace.kernel.model.AuthenticatedUser;
import onlydust.com.marketplace.kernel.model.ProjectId;
import onlydust.com.marketplace.kernel.model.UserId;
import onlydust.com.marketplace.kernel.port.output.NotificationPort;
import onlydust.com.marketplace.project.domain.model.GithubIssue;
import onlydust.com.marketplace.project.domain.model.Project;
import onlydust.com.marketplace.project.domain.model.notification.GoodFirstIssueCreated;
import onlydust.com.marketplace.project.domain.model.notification.dto.NotificationDetailedIssue;
import onlydust.com.marketplace.project.domain.model.notification.dto.NotificationProject;
import onlydust.com.marketplace.project.domain.port.output.GithubStoragePort;
import onlydust.com.marketplace.project.domain.port.output.ProjectStoragePort;
import onlydust.com.marketplace.project.domain.port.output.UserStoragePort;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.Mockito.*;

public class GoodFirstIssueCreatedNotifierJobTest {

    private final Faker faker = new Faker();

    @Test
    void should_notify_users() {
        // Given
        final GithubStoragePort githubStoragePort = mock(GithubStoragePort.class);
        final ProjectStoragePort projectStoragePort = mock(ProjectStoragePort.class);
        final UserStoragePort userStoragePort = mock(UserStoragePort.class);
        final NotificationPort notificationPort = mock(NotificationPort.class);
        final GoodFirstIssueCreatedNotifierJob goodFirstIssueCreatedNotifierJob = new GoodFirstIssueCreatedNotifierJob(githubStoragePort,
                projectStoragePort, userStoragePort, notificationPort);
        final ProjectId projectId1 = ProjectId.random();
        final ProjectId projectId2 = ProjectId.random();
        final Project project1 = Project.builder()
                .id(projectId1)
                .slug(faker.lorem().word())
                .name(faker.lorem().word())
                .build();
        final Project project2 = Project.builder()
                .id(projectId2)
                .slug(faker.lorem().word())
                .name(faker.lorem().word())
                .build();
        final AuthenticatedUser user1 = AuthenticatedUser.builder()
                .id(UserId.random())
                .build();
        final AuthenticatedUser user2 = AuthenticatedUser.builder()
                .id(UserId.random())
                .build();
        final GithubIssue githubIssue1 = new GithubIssue(GithubIssue.Id.random(), 1L, 1L, "title1", null, faker.internet().url(),
                faker.rickAndMorty().character(), 1, faker.pokemon().name(), faker.pokemon().name(), List.of());
        final GithubIssue githubIssue2 = new GithubIssue(GithubIssue.Id.random(), 2L, 2L, "title2", null, faker.internet().url(),
                faker.rickAndMorty().character(), 1, faker.pokemon().name(), faker.pokemon().name(), List.of());

        // When
        when(githubStoragePort.findGoodFirstIssuesCreatedSince5Minutes())
                .thenReturn(List.of(
                        githubIssue1,
                        githubIssue2
                ));
        when(projectStoragePort.findProjectIdsByRepoId(1L)).thenReturn(List.of(projectId1, projectId2));
        when(projectStoragePort.findProjectIdsByRepoId(2L)).thenReturn(List.of());
        when(projectStoragePort.getById(projectId1)).thenReturn(Optional.of(project1));
        when(projectStoragePort.getById(projectId2)).thenReturn(Optional.of(project2));
        when(userStoragePort.findUserIdsRegisteredOnNotifyOnNewGoodFirstIssuesOnProject(projectId1))
                .thenReturn(List.of(user1.id(), user2.id()));
        when(userStoragePort.findUserIdsRegisteredOnNotifyOnNewGoodFirstIssuesOnProject(projectId2))
                .thenReturn(List.of());


        goodFirstIssueCreatedNotifierJob.run();

        // Then
        final NotificationProject notificationProject = new NotificationProject(project1.getId(), project1.getSlug(), project1.getName());
        final NotificationDetailedIssue notificationDetailedIssue = new NotificationDetailedIssue(githubIssue1.id().value(), githubIssue1.htmlUrl(),
                githubIssue1.title(),
                githubIssue1.repoName(),
                githubIssue1.description(), githubIssue1.authorLogin(), githubIssue1.authorAvatarUrl(), githubIssue1.labels());
        verify(notificationPort).push(user1.id(), new GoodFirstIssueCreated(notificationProject, notificationDetailedIssue));
        verify(notificationPort).push(user2.id(), new GoodFirstIssueCreated(notificationProject, notificationDetailedIssue));
        verifyNoMoreInteractions(notificationPort);
    }

    @Test
    void should_prevent_to_notify_users_given_a_project_not_found() {
        // Given
        final GithubStoragePort githubStoragePort = mock(GithubStoragePort.class);
        final ProjectStoragePort projectStoragePort = mock(ProjectStoragePort.class);
        final UserStoragePort userStoragePort = mock(UserStoragePort.class);
        final NotificationPort notificationPort = mock(NotificationPort.class);
        final GoodFirstIssueCreatedNotifierJob goodFirstIssueCreatedNotifierJob = new GoodFirstIssueCreatedNotifierJob(githubStoragePort,
                projectStoragePort, userStoragePort, notificationPort);
        final ProjectId projectId1 = ProjectId.random();
        final GithubIssue githubIssue1 = new GithubIssue(GithubIssue.Id.random(), 1L, 1L, "title1", null, faker.internet().url(),
                faker.rickAndMorty().character(), 1, faker.pokemon().name(), faker.pokemon().name(), List.of());

        // When
        when(githubStoragePort.findGoodFirstIssuesCreatedSince5Minutes())
                .thenReturn(List.of(
                        githubIssue1
                ));
        when(projectStoragePort.findProjectIdsByRepoId(1L)).thenReturn(List.of(projectId1));
        when(projectStoragePort.getById(projectId1)).thenReturn(Optional.empty());
        Exception exception = null;
        try {
            goodFirstIssueCreatedNotifierJob.run();
        } catch (Exception e) {
            exception = e;
        }

        // Then
        assertInstanceOf(OnlyDustException.class, exception);
        assertEquals(500, ((OnlyDustException) exception).getStatus());
        assertEquals("Project %s not found".formatted(projectId1), exception.getMessage());
        verifyNoMoreInteractions(notificationPort);
    }


}
