package onlydust.com.marketplace.project.domain.service;

import onlydust.com.marketplace.kernel.model.ContributionUUID;
import onlydust.com.marketplace.kernel.model.UserId;
import onlydust.com.marketplace.kernel.port.output.PermissionPort;
import onlydust.com.marketplace.project.domain.model.UpdatePullRequestCommand;
import onlydust.com.marketplace.project.domain.port.input.ContributionObserverPort;
import onlydust.com.marketplace.project.domain.port.output.ContributionStoragePort;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

public class PullRequestServiceTest {

    private PullRequestService pullRequestService;
    private PermissionPort permissionPort;
    private ContributionStoragePort contributionStoragePort;
    private ContributionObserverPort contributionObserverPort;

    @BeforeEach
    public void setUp() {
        permissionPort = mock(PermissionPort.class);
        contributionStoragePort = mock(ContributionStoragePort.class);
        contributionObserverPort = mock(ContributionObserverPort.class);
        pullRequestService = new PullRequestService(permissionPort, contributionStoragePort, contributionObserverPort);
    }

    @Test
    void should_archive_pull_request() {
        // Given
        final var userId = UserId.random();
        final var contributionUuid = ContributionUUID.random();

        // When
        when(permissionPort.canUserUpdateContribution(userId, contributionUuid)).thenReturn(true);
        pullRequestService.updatePullRequest(userId, new UpdatePullRequestCommand(contributionUuid, false));

        // Then
        verify(contributionStoragePort).archiveContribution(contributionUuid, false);
        verify(contributionObserverPort).onContributionsChanged(contributionUuid);
    }

    @Test
    void should_forbid_user_not_project_lead_to_update_a_pull_request() {
        // Given
        final var userId = UserId.random();
        final var contributionUuid = ContributionUUID.random();

        // When
        when(permissionPort.canUserUpdateContribution(userId, contributionUuid)).thenReturn(false);
        Exception exception = null;
        try {
            pullRequestService.updatePullRequest(userId, new UpdatePullRequestCommand(contributionUuid, false));
        } catch (Exception e) {
            exception = e;
        }

        // Then
        Assertions.assertNotNull(exception);
        Assertions.assertEquals(String.format("User %s must be project lead to update pull request %s linked to its projects", userId, contributionUuid),
                exception.getMessage());
    }
}
