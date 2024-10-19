package onlydust.com.marketplace.project.domain.service;

import com.github.javafaker.Faker;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import onlydust.com.marketplace.kernel.model.UserId;
import onlydust.com.marketplace.kernel.port.output.PermissionPort;
import onlydust.com.marketplace.project.domain.model.GithubAppAccessToken;
import onlydust.com.marketplace.project.domain.model.GithubIssue;
import onlydust.com.marketplace.project.domain.model.UpdateIssueCommand;
import onlydust.com.marketplace.project.domain.port.output.ContributionStoragePort;
import onlydust.com.marketplace.project.domain.port.output.GithubApiPort;
import onlydust.com.marketplace.project.domain.port.output.GithubStoragePort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class IssueServiceTest {
    private IssueService issueService;
    private PermissionPort permissionPort;
    private ContributionStoragePort contributionStoragePort;
    private GithubStoragePort githubStoragePort;
    private GithubAppService githubAppService;
    private GithubApiPort githubApiPort;
    private final Faker faker = new Faker();

    @BeforeEach
    public void setUp() {
        permissionPort = mock(PermissionPort.class);
        contributionStoragePort = mock(ContributionStoragePort.class);
        githubStoragePort = mock(GithubStoragePort.class);
        githubAppService = mock(GithubAppService.class);
        githubApiPort = mock(GithubApiPort.class);
        issueService = new IssueService(permissionPort, contributionStoragePort, githubApiPort, githubStoragePort, githubAppService);
    }

    @Test
    void should_archive_issue() {
        // Given
        final UserId userId = UserId.random();
        final long issueId = 1L;

        // When
        when(permissionPort.canUserUpdateIssue(userId, issueId)).thenReturn(true);
        issueService.updateIssue(userId, new UpdateIssueCommand(GithubIssue.Id.of(issueId), false, null));

        // Then
        verify(contributionStoragePort).archiveIssue(GithubIssue.Id.of(issueId), false);
    }

    @Test
    void should_forbid_user_not_project_lead_to_update_an_issue() {
        // Given
        final UserId userId = UserId.random();
        final long issueId = 1L;

        // When
        when(permissionPort.canUserUpdateIssue(userId, issueId)).thenReturn(false);
        Exception exception = null;
        try {
            issueService.updateIssue(userId, new UpdateIssueCommand(GithubIssue.Id.of(issueId), false, null));
        } catch (Exception e) {
            exception = e;
        }

        // Then
        assertNotNull(exception);
        assertEquals(String.format("User %s must be project lead to update issue %s linked to its projects", userId, issueId),
                exception.getMessage());
    }

    @Test
    void should_close_issue() {
        // Given
        final UserId userId = UserId.random();
        final GithubIssue.Id issueId = GithubIssue.Id.of(1L);
        final GithubIssue githubIssue = new GithubIssue(issueId, faker.random().nextLong(), faker.random().nextLong(), faker.rickAndMorty().character(), null,
                faker.internet().url(), faker.rickAndMorty().character(), 0, null, null, null);
        final GithubAppAccessToken githubAppAccessToken = new GithubAppAccessToken(faker.ancient().god(), Map.of(
                "issues", "write"));

        // When
        when(permissionPort.canUserUpdateIssue(userId, issueId.value())).thenReturn(true);
        when(githubStoragePort.findIssueById(issueId)).thenReturn(Optional.of(githubIssue));
        when(githubAppService.getInstallationTokenFor(githubIssue.repoId())).thenReturn(Optional.of(githubAppAccessToken));
        issueService.updateIssue(userId, new UpdateIssueCommand(GithubIssue.Id.of(1L), null, true));

        // Then
        verifyNoInteractions(contributionStoragePort);
        verify(githubApiPort).closeIssue(githubAppAccessToken.token(), githubIssue.repoId(), githubIssue.number());
    }

    @Test
    void should_not_close_issue_given_issue_not_found() {
        // Given
        final UserId userId = UserId.random();
        final GithubIssue.Id issueId = GithubIssue.Id.of(1L);

        // When
        when(permissionPort.canUserUpdateIssue(userId, issueId.value())).thenReturn(true);
        when(githubStoragePort.findIssueById(issueId)).thenReturn(Optional.empty());

        // Then
        assertThrows(OnlyDustException.class, () -> issueService.updateIssue(userId, new UpdateIssueCommand(issueId, null, true)), "Issue not found");
        verifyNoInteractions(githubApiPort);
    }

    @Test
    void should_not_close_issue_given_github_app_token_not_generated() {
        // Given
        final UserId userId = UserId.random();
        final GithubIssue.Id issueId = GithubIssue.Id.of(1L);
        final GithubIssue githubIssue = new GithubIssue(issueId, faker.random().nextLong(), faker.random().nextLong(), faker.rickAndMorty().character(), null,
                faker.internet().url(), faker.rickAndMorty().character(), 0, null, null, null);

        // When
        when(permissionPort.canUserUpdateIssue(userId, issueId.value())).thenReturn(true);
        when(githubStoragePort.findIssueById(issueId)).thenReturn(Optional.of(githubIssue));
        when(githubAppService.getInstallationTokenFor(githubIssue.repoId())).thenReturn(Optional.empty());

        // Then
        assertThrows(OnlyDustException.class, () -> issueService.updateIssue(userId, new UpdateIssueCommand(issueId, null, true)),
                "Could not generate GitHub App token for repository %d".formatted(githubIssue.repoId()));
        verifyNoInteractions(githubApiPort);
    }

    @Test
    void should_not_close_issue_given_github_app_has_missing_permissions() {
        // Given
        final UserId userId = UserId.random();
        final GithubIssue.Id issueId = GithubIssue.Id.of(1L);
        final GithubIssue githubIssue = new GithubIssue(issueId, faker.random().nextLong(), faker.random().nextLong(), faker.rickAndMorty().character(), null,
                faker.internet().url(), faker.rickAndMorty().character(), 0, null, null, null);
        final GithubAppAccessToken githubAppAccessToken = new GithubAppAccessToken(faker.ancient().god(), Map.of(
                "issues", "read"));

        // When
        when(permissionPort.canUserUpdateIssue(userId, issueId.value())).thenReturn(true);
        when(githubStoragePort.findIssueById(issueId)).thenReturn(Optional.of(githubIssue));
        when(githubAppService.getInstallationTokenFor(githubIssue.repoId())).thenReturn(Optional.of(githubAppAccessToken));

        // Then
        assertThrows(OnlyDustException.class, () -> issueService.updateIssue(userId, new UpdateIssueCommand(issueId, null, true)),
                "Github app installed on repo %s has not the permission to write on issue".formatted(githubIssue.repoId()));
        verifyNoInteractions(githubApiPort);
    }


}
