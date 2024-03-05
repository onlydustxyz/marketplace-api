package onlydust.com.marketplace.project.domain.service;

import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import onlydust.com.marketplace.kernel.port.output.IndexerPort;
import onlydust.com.marketplace.project.domain.model.Currency;
import onlydust.com.marketplace.project.domain.model.OldRequestRewardCommand;
import onlydust.com.marketplace.project.domain.port.output.ProjectRewardStoragePort;
import onlydust.com.marketplace.project.domain.port.output.RewardServicePort;
import onlydust.com.marketplace.project.domain.port.output.UserStoragePort;
import onlydust.com.marketplace.project.domain.view.BudgetView;
import onlydust.com.marketplace.project.domain.view.ProjectBudgetsView;
import onlydust.com.marketplace.project.domain.view.RewardView;
import onlydust.com.marketplace.project.domain.view.UserRewardView;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class RewardServiceTest {

    @Test
    void should_request_reward_given_a_project_lead() {
        // Given
        final RewardServicePort rewardServicePort = mock(RewardServicePort.class);
        final PermissionService permissionService = mock(PermissionService.class);
        final ProjectRewardStoragePort projectRewardStoragePort = mock(ProjectRewardStoragePort.class);
        final IndexerPort indexerPort = mock(IndexerPort.class);
        final UserStoragePort userStoragePort = mock(UserStoragePort.class);

        final RewardService rewardService =
                new RewardService(rewardServicePort, projectRewardStoragePort, permissionService, indexerPort,
                        userStoragePort);

        final UUID projectLeadId = UUID.randomUUID();
        final OldRequestRewardCommand oldRequestRewardCommand =
                OldRequestRewardCommand.builder()
                        .projectId(UUID.randomUUID())
                        .amount(BigDecimal.valueOf(10L))
                        .currency(Currency.STRK)
                        .build();
        final var newRewardId = UUID.randomUUID();

        // When
        when(rewardServicePort.create(projectLeadId, oldRequestRewardCommand))
                .thenReturn(newRewardId);
        when(permissionService.isUserProjectLead(oldRequestRewardCommand.getProjectId(), projectLeadId))
                .thenReturn(true);
        when(projectRewardStoragePort.findBudgets(oldRequestRewardCommand.getProjectId()))
                .thenReturn(ProjectBudgetsView.builder()
                        .budgets(List.of(BudgetView.builder()
                                .currency(Currency.STRK)
                                .remaining(BigDecimal.valueOf(100L))
                                .build()))
                        .build());
        final UUID rewardId = rewardService.createReward(projectLeadId,
                oldRequestRewardCommand);

        // Then
        assertThat(rewardId).isEqualTo(newRewardId);
        verify(indexerPort, times(1)).indexUser(oldRequestRewardCommand.getRecipientId());
    }

    @Test
    void should_throw_a_forbidden_exception_given_not_a_project_lead() {
        // Given
        final RewardServicePort rewardServicePort = mock(RewardServicePort.class);
        final PermissionService permissionService = mock(PermissionService.class);
        final ProjectRewardStoragePort projectRewardStoragePort = mock(ProjectRewardStoragePort.class);
        final IndexerPort indexerPort = mock(IndexerPort.class);
        final UserStoragePort userStoragePort = mock(UserStoragePort.class);

        final RewardService rewardService =
                new RewardService(rewardServicePort, projectRewardStoragePort, permissionService, indexerPort,
                        userStoragePort);

        final UUID projectLeadId = UUID.randomUUID();
        final OldRequestRewardCommand oldRequestRewardCommand =
                OldRequestRewardCommand.builder().projectId(UUID.randomUUID())
                        .amount(BigDecimal.valueOf(10L))
                        .currency(Currency.STRK)
                        .build();

        // When
        when(permissionService.isUserProjectLead(oldRequestRewardCommand.getProjectId(), projectLeadId))
                .thenReturn(false);
        when(projectRewardStoragePort.findBudgets(oldRequestRewardCommand.getProjectId()))
                .thenReturn(ProjectBudgetsView.builder()
                        .budgets(List.of(BudgetView.builder()
                                .currency(Currency.STRK)
                                .remaining(BigDecimal.valueOf(100L))
                                .build()))
                        .build());
        OnlyDustException onlyDustException = null;
        try {
            rewardService.createReward(projectLeadId, oldRequestRewardCommand);
        } catch (OnlyDustException e) {
            onlyDustException = e;
        }

        // Then
        Assertions.assertNotNull(onlyDustException);
        Assertions.assertEquals(403, onlyDustException.getStatus());
        Assertions.assertEquals("User must be project lead to request a reward", onlyDustException.getMessage());
        verify(indexerPort, never()).indexUser(oldRequestRewardCommand.getRecipientId());
    }

    @Test
    void should_throw_a_forbidden_exception_given_not_amount_equals_to_0() {
        // Given
        final RewardServicePort rewardServicePort = mock(RewardServicePort.class);
        final PermissionService permissionService = mock(PermissionService.class);
        final ProjectRewardStoragePort projectRewardStoragePort = mock(ProjectRewardStoragePort.class);
        final IndexerPort indexerPort = mock(IndexerPort.class);
        final UserStoragePort userStoragePort = mock(UserStoragePort.class);

        final RewardService rewardService =
                new RewardService(rewardServicePort, projectRewardStoragePort, permissionService, indexerPort,
                        userStoragePort);

        final UUID projectLeadId = UUID.randomUUID();
        final OldRequestRewardCommand oldRequestRewardCommand =
                OldRequestRewardCommand.builder().projectId(UUID.randomUUID())
                        .amount(BigDecimal.valueOf(0L))
                        .currency(Currency.STRK)
                        .build();

        // When
        when(permissionService.isUserProjectLead(oldRequestRewardCommand.getProjectId(), projectLeadId))
                .thenReturn(true);
        when(projectRewardStoragePort.findBudgets(oldRequestRewardCommand.getProjectId()))
                .thenReturn(ProjectBudgetsView.builder()
                        .budgets(List.of(BudgetView.builder()
                                .currency(Currency.STRK)
                                .remaining(BigDecimal.valueOf(100L))
                                .build()))
                        .build());
        OnlyDustException onlyDustException = null;
        try {
            rewardService.createReward(projectLeadId, oldRequestRewardCommand);
        } catch (OnlyDustException e) {
            onlyDustException = e;
        }

        // Then
        Assertions.assertNotNull(onlyDustException);
        Assertions.assertEquals(403, onlyDustException.getStatus());
        Assertions.assertEquals("Amount must be greater than 0", onlyDustException.getMessage());
        verify(indexerPort, never()).indexUser(oldRequestRewardCommand.getRecipientId());
    }


    @Test
    void should_throw_a_invalid_input_exception_when_there_is_no_budget_of_such_currency() {
        // Given
        final RewardServicePort rewardServicePort = mock(RewardServicePort.class);
        final PermissionService permissionService = mock(PermissionService.class);
        final ProjectRewardStoragePort projectRewardStoragePort = mock(ProjectRewardStoragePort.class);
        final IndexerPort indexerPort = mock(IndexerPort.class);
        final UserStoragePort userStoragePort = mock(UserStoragePort.class);

        final RewardService rewardService =
                new RewardService(rewardServicePort, projectRewardStoragePort, permissionService, indexerPort,
                        userStoragePort);

        final UUID projectLeadId = UUID.randomUUID();
        final OldRequestRewardCommand oldRequestRewardCommand =
                OldRequestRewardCommand.builder().projectId(UUID.randomUUID())
                        .amount(BigDecimal.valueOf(10L))
                        .currency(Currency.STRK)
                        .build();

        // When
        when(permissionService.isUserProjectLead(oldRequestRewardCommand.getProjectId(), projectLeadId))
                .thenReturn(true);
        when(projectRewardStoragePort.findBudgets(oldRequestRewardCommand.getProjectId()))
                .thenReturn(ProjectBudgetsView.builder()
                        .budgets(List.of())
                        .build());
        OnlyDustException onlyDustException = null;
        try {
            rewardService.createReward(projectLeadId, oldRequestRewardCommand);
        } catch (OnlyDustException e) {
            onlyDustException = e;
        }

        // Then
        Assertions.assertNotNull(onlyDustException);
        Assertions.assertEquals(400, onlyDustException.getStatus());
        Assertions.assertEquals(("Not enough budget of currency STRK for project %s to request a reward with an " +
                                 "amount of 10").formatted(oldRequestRewardCommand.getProjectId()),
                onlyDustException.getMessage());
        verify(indexerPort, never()).indexUser(oldRequestRewardCommand.getRecipientId());
    }

    @Test
    void should_throw_a_invalid_input_exception_when_there_is_not_enough_budget_of_such_currency() {
        // Given
        final RewardServicePort rewardServicePort = mock(RewardServicePort.class);
        final PermissionService permissionService = mock(PermissionService.class);
        final ProjectRewardStoragePort projectRewardStoragePort = mock(ProjectRewardStoragePort.class);
        final IndexerPort indexerPort = mock(IndexerPort.class);
        final UserStoragePort userStoragePort = mock(UserStoragePort.class);

        final RewardService rewardService =
                new RewardService(rewardServicePort, projectRewardStoragePort, permissionService, indexerPort,
                        userStoragePort);

        final UUID projectLeadId = UUID.randomUUID();
        final OldRequestRewardCommand oldRequestRewardCommand =
                OldRequestRewardCommand.builder().projectId(UUID.randomUUID())
                        .amount(BigDecimal.valueOf(10L))
                        .currency(Currency.STRK)
                        .build();

        // When
        when(permissionService.isUserProjectLead(oldRequestRewardCommand.getProjectId(), projectLeadId))
                .thenReturn(true);
        when(projectRewardStoragePort.findBudgets(oldRequestRewardCommand.getProjectId()))
                .thenReturn(ProjectBudgetsView.builder()
                        .budgets(List.of(BudgetView.builder()
                                .currency(Currency.STRK)
                                .remaining(BigDecimal.valueOf(9L))
                                .build()))
                        .build());
        OnlyDustException onlyDustException = null;
        try {
            rewardService.createReward(projectLeadId, oldRequestRewardCommand);
        } catch (OnlyDustException e) {
            onlyDustException = e;
        }

        // Then
        Assertions.assertNotNull(onlyDustException);
        Assertions.assertEquals(400, onlyDustException.getStatus());
        Assertions.assertEquals(("Not enough budget of currency STRK for project %s to request a reward with an " +
                                 "amount of 10").formatted(oldRequestRewardCommand.getProjectId()),
                onlyDustException.getMessage());
        verify(indexerPort, never()).indexUser(oldRequestRewardCommand.getRecipientId());
    }

    @Test
    void should_cancel_reward_given_a_project_lead() {
        // Given
        final RewardServicePort rewardServicePort = mock(RewardServicePort.class);
        final PermissionService permissionService = mock(PermissionService.class);
        final ProjectRewardStoragePort projectRewardStoragePort = mock(ProjectRewardStoragePort.class);
        final IndexerPort indexerPort = mock(IndexerPort.class);
        final UserStoragePort userStoragePort = mock(UserStoragePort.class);

        final RewardService rewardService =
                new RewardService(rewardServicePort, projectRewardStoragePort, permissionService, indexerPort,
                        userStoragePort);

        final UUID projectLeadId = UUID.randomUUID();
        final UUID projectId = UUID.randomUUID();
        final var rewardId = UUID.randomUUID();

        // When
        when(permissionService.isUserProjectLead(projectId, projectLeadId))
                .thenReturn(true);
        rewardService.cancelReward(projectLeadId, projectId, rewardId);

        // Then
        verify(rewardServicePort).cancel(rewardId);
    }

    @Test
    void should_throw_a_forbidden_exception_when_cancelling_reward_given_not_a_project_lead() {
        // Given
        final RewardServicePort rewardServicePort = mock(RewardServicePort.class);
        final PermissionService permissionService = mock(PermissionService.class);
        final ProjectRewardStoragePort projectRewardStoragePort = mock(ProjectRewardStoragePort.class);
        final IndexerPort indexerPort = mock(IndexerPort.class);
        final UserStoragePort userStoragePort = mock(UserStoragePort.class);

        final RewardService rewardService =
                new RewardService(rewardServicePort, projectRewardStoragePort, permissionService, indexerPort,
                        userStoragePort);

        final UUID projectLeadId = UUID.randomUUID();
        final UUID projectId = UUID.randomUUID();
        final var rewardId = UUID.randomUUID();

        // When
        when(permissionService.isUserProjectLead(projectId, projectLeadId))
                .thenReturn(false);
        OnlyDustException onlyDustException = null;
        try {
            rewardService.cancelReward(projectLeadId, projectId, rewardId);
        } catch (OnlyDustException e) {
            onlyDustException = e;
        }

        // Then
        Assertions.assertNotNull(onlyDustException);
        Assertions.assertEquals(403, onlyDustException.getStatus());
        Assertions.assertEquals("User must be project lead to cancel a reward", onlyDustException.getMessage());
    }

    @Test
    void should_throw_a_forbidden_exception_when_cancelling_a_reward_already_contained_in_an_invoice() {
        final RewardServicePort rewardServicePort = mock(RewardServicePort.class);
        final PermissionService permissionService = mock(PermissionService.class);
        final ProjectRewardStoragePort projectRewardStoragePort = mock(ProjectRewardStoragePort.class);
        final IndexerPort indexerPort = mock(IndexerPort.class);
        final UserStoragePort userStoragePort = mock(UserStoragePort.class);

        final RewardService rewardService =
                new RewardService(rewardServicePort, projectRewardStoragePort, permissionService, indexerPort,
                        userStoragePort);

        final UUID projectLeadId = UUID.randomUUID();
        final UUID projectId = UUID.randomUUID();
        final var rewardId = UUID.randomUUID();

        // When
        when(permissionService.isUserProjectLead(projectId, projectLeadId))
                .thenReturn(true);
        when(userStoragePort.findRewardById(rewardId))
                .thenReturn(RewardView.builder().id(rewardId).invoiceId(UUID.randomUUID()).build());
        OnlyDustException onlyDustException = null;
        try {
            rewardService.cancelReward(projectLeadId, projectId, rewardId);
        } catch (OnlyDustException e) {
            onlyDustException = e;
        }

        // Then
        Assertions.assertNotNull(onlyDustException);
        Assertions.assertEquals(403, onlyDustException.getStatus());
        Assertions.assertEquals("Cannot cancel reward %s which is already contained in an invoice".formatted(rewardId), onlyDustException.getMessage());
    }

    @Test
    void should_mark_invoice_as_received() {
        // Given
        final RewardServicePort rewardServicePort = mock(RewardServicePort.class);
        final PermissionService permissionService = mock(PermissionService.class);
        final ProjectRewardStoragePort projectRewardStoragePort = mock(ProjectRewardStoragePort.class);
        final IndexerPort indexerPort = mock(IndexerPort.class);
        final UserStoragePort userStoragePort = mock(UserStoragePort.class);

        final RewardService rewardService =
                new RewardService(rewardServicePort, projectRewardStoragePort, permissionService, indexerPort,
                        userStoragePort);
        final var rewardIds = List.of(UUID.randomUUID(), UUID.randomUUID());

        // When
        when(userStoragePort.findPendingInvoiceRewardsForRecipientId(1L))
                .thenReturn(List.of(UserRewardView.builder()
                        .id(rewardIds.get(0))
                        .build(), UserRewardView.builder()
                        .id(rewardIds.get(1))
                        .build()));
        rewardService.markInvoiceAsReceived(1L);

        // Then
        verify(rewardServicePort).markInvoiceAsReceived(rewardIds);
    }

}
