package onlydust.com.marketplace.project.domain.service;

import onlydust.com.marketplace.kernel.model.UserId;
import onlydust.com.marketplace.kernel.port.output.PermissionPort;
import onlydust.com.marketplace.project.domain.model.GithubPullRequest;
import onlydust.com.marketplace.project.domain.model.UpdatePullRequestCommand;
import onlydust.com.marketplace.project.domain.port.output.ContributionStoragePort;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.mockito.Mockito.*;

public class PullRequestServiceTest {

    private PullRequestService pullRequestService;
    private PermissionPort permissionPort;
    private ContributionStoragePort contributionStoragePort;

    @BeforeEach
    public void setUp() {
        permissionPort = mock(PermissionPort.class);
        contributionStoragePort = mock(ContributionStoragePort.class);
        pullRequestService = new PullRequestService(permissionPort, contributionStoragePort);
    }

    @Test
    void should_archive_pull_request() {
        // Given
        final UserId userId = UserId.random();
        final long pullRequestId = 1L;

        // When
        when(permissionPort.canUserUpdatePullRequest(userId, pullRequestId)).thenReturn(true);
        pullRequestService.updatePullRequest(userId, new UpdatePullRequestCommand(GithubPullRequest.Id.of(pullRequestId), false, List.of()));

        // Then
        verify(contributionStoragePort).archivePullRequest(GithubPullRequest.Id.of(pullRequestId), false);
    }

    @Test
    void should_forbid_user_not_project_lead_to_update_a_pull_request() {
        // Given
        final UserId userId = UserId.random();
        final long issueId = 1L;

        // When
        when(permissionPort.canUserUpdatePullRequest(userId, issueId)).thenReturn(false);
        Exception exception = null;
        try {
            pullRequestService.updatePullRequest(userId, new UpdatePullRequestCommand(GithubPullRequest.Id.of(issueId), false, List.of()));
        } catch (Exception e) {
            exception = e;
        }

        // Then
        Assertions.assertNotNull(exception);
        Assertions.assertEquals(String.format("User %s must be project lead to update pull request %s linked to its projects", userId, issueId),
                exception.getMessage());
    }
}
