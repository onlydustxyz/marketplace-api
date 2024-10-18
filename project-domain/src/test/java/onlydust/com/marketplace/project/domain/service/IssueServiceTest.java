package onlydust.com.marketplace.project.domain.service;

import onlydust.com.marketplace.kernel.model.ContributionUUID;
import onlydust.com.marketplace.kernel.model.UserId;
import onlydust.com.marketplace.kernel.port.output.PermissionPort;
import onlydust.com.marketplace.project.domain.model.UpdateIssueCommand;
import onlydust.com.marketplace.project.domain.port.output.ContributionStoragePort;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

public class IssueServiceTest {
    private IssueService issueService;
    private PermissionPort permissionPort;
    private ContributionStoragePort contributionStoragePort;

    @BeforeEach
    public void setUp() {
        permissionPort = mock(PermissionPort.class);
        contributionStoragePort = mock(ContributionStoragePort.class);
        issueService = new IssueService(permissionPort, contributionStoragePort);
    }

    @Test
    void should_archive_issue() {
        // Given
        final var userId = UserId.random();
        final var contributionUuid = ContributionUUID.random();

        // When
        when(permissionPort.canUserUpdateContribution(userId, contributionUuid)).thenReturn(true);
        issueService.updateIssue(userId, new UpdateIssueCommand(contributionUuid, false));

        // Then
        verify(contributionStoragePort).archiveContribution(contributionUuid, false);
    }

    @Test
    void should_forbid_user_not_project_lead_to_update_an_issue() {
        // Given
        final var userId = UserId.random();
        final var contributionUuid = ContributionUUID.random();

        // When
        when(permissionPort.canUserUpdateContribution(userId, contributionUuid)).thenReturn(false);
        Exception exception = null;
        try {
            issueService.updateIssue(userId, new UpdateIssueCommand(contributionUuid, false));
        } catch (Exception e) {
            exception = e;
        }

        // Then
        Assertions.assertNotNull(exception);
        Assertions.assertEquals(String.format("User %s must be project lead to update issue %s linked to its projects", userId, contributionUuid),
                exception.getMessage());
    }
}
