package onlydust.com.marketplace.api.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import onlydust.com.marketplace.api.domain.model.Currency;
import onlydust.com.marketplace.api.domain.model.RequestRewardCommand;
import onlydust.com.marketplace.api.domain.port.output.IndexerPort;
import onlydust.com.marketplace.api.domain.port.output.ProjectStoragePort;
import onlydust.com.marketplace.api.domain.port.output.RewardServicePort;
import onlydust.com.marketplace.api.domain.port.output.UserStoragePort;
import onlydust.com.marketplace.api.domain.view.BudgetView;
import onlydust.com.marketplace.api.domain.view.ProjectBudgetsView;
import onlydust.com.marketplace.api.domain.view.UserRewardView;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class RewardServiceTest {

  @Test
  void should_request_reward_given_a_project_lead() {
    // Given
    final RewardServicePort rewardServicePort = mock(RewardServicePort.class);
    final PermissionService permissionService = mock(PermissionService.class);
    final ProjectStoragePort projectStoragePort = mock(ProjectStoragePort.class);
    final IndexerPort indexerPort = mock(IndexerPort.class);
    final UserStoragePort userStoragePort = mock(UserStoragePort.class);

    final RewardService rewardService =
        new RewardService(rewardServicePort, projectStoragePort, permissionService, indexerPort,
            userStoragePort);

    final UUID projectLeadId = UUID.randomUUID();
    final RequestRewardCommand requestRewardCommand =
        RequestRewardCommand.builder()
            .projectId(UUID.randomUUID())
            .amount(BigDecimal.valueOf(10L))
            .currency(Currency.Strk)
            .build();
    final var newRewardId = UUID.randomUUID();

    // When
    when(rewardServicePort.requestPayment(projectLeadId, requestRewardCommand))
        .thenReturn(newRewardId);
    when(permissionService.isUserProjectLead(requestRewardCommand.getProjectId(), projectLeadId))
        .thenReturn(true);
    when(projectStoragePort.findBudgets(requestRewardCommand.getProjectId()))
        .thenReturn(ProjectBudgetsView.builder()
            .budgets(List.of(BudgetView.builder()
                .currency(Currency.Strk)
                .remaining(BigDecimal.valueOf(100L))
                .build()))
            .build());
    final UUID rewardId = rewardService.requestPayment(projectLeadId,
        requestRewardCommand);

    // Then
    assertThat(rewardId).isEqualTo(newRewardId);
    verify(indexerPort, times(1)).indexUser(requestRewardCommand.getRecipientId());
  }

  @Test
  void should_throw_a_forbidden_exception_given_not_a_project_lead() {
    // Given
    final RewardServicePort rewardServicePort = mock(RewardServicePort.class);
    final PermissionService permissionService = mock(PermissionService.class);
    final ProjectStoragePort projectStoragePort = mock(ProjectStoragePort.class);
    final IndexerPort indexerPort = mock(IndexerPort.class);
    final UserStoragePort userStoragePort = mock(UserStoragePort.class);

    final RewardService rewardService =
        new RewardService(rewardServicePort, projectStoragePort, permissionService, indexerPort,
            userStoragePort);

    final UUID projectLeadId = UUID.randomUUID();
    final RequestRewardCommand requestRewardCommand =
        RequestRewardCommand.builder().projectId(UUID.randomUUID())
            .amount(BigDecimal.valueOf(10L))
            .currency(Currency.Strk)
            .build();

    // When
    when(permissionService.isUserProjectLead(requestRewardCommand.getProjectId(), projectLeadId))
        .thenReturn(false);
    when(projectStoragePort.findBudgets(requestRewardCommand.getProjectId()))
        .thenReturn(ProjectBudgetsView.builder()
            .budgets(List.of(BudgetView.builder()
                .currency(Currency.Strk)
                .remaining(BigDecimal.valueOf(100L))
                .build()))
            .build());
    OnlyDustException onlyDustException = null;
    try {
      rewardService.requestPayment(projectLeadId, requestRewardCommand);
    } catch (OnlyDustException e) {
      onlyDustException = e;
    }

    // Then
    Assertions.assertNotNull(onlyDustException);
    Assertions.assertEquals(403, onlyDustException.getStatus());
    Assertions.assertEquals("User must be project lead to request a reward", onlyDustException.getMessage());
    verify(indexerPort, never()).indexUser(requestRewardCommand.getRecipientId());
  }

  @Test
  void should_throw_a_forbidden_exception_given_not_amount_equals_to_0() {
    // Given
    final RewardServicePort rewardServicePort = mock(RewardServicePort.class);
    final PermissionService permissionService = mock(PermissionService.class);
    final ProjectStoragePort projectStoragePort = mock(ProjectStoragePort.class);
    final IndexerPort indexerPort = mock(IndexerPort.class);
    final UserStoragePort userStoragePort = mock(UserStoragePort.class);

    final RewardService rewardService =
        new RewardService(rewardServicePort, projectStoragePort, permissionService, indexerPort,
            userStoragePort);

    final UUID projectLeadId = UUID.randomUUID();
    final RequestRewardCommand requestRewardCommand =
        RequestRewardCommand.builder().projectId(UUID.randomUUID())
            .amount(BigDecimal.valueOf(0L))
            .currency(Currency.Strk)
            .build();

    // When
    when(permissionService.isUserProjectLead(requestRewardCommand.getProjectId(), projectLeadId))
        .thenReturn(true);
    when(projectStoragePort.findBudgets(requestRewardCommand.getProjectId()))
        .thenReturn(ProjectBudgetsView.builder()
            .budgets(List.of(BudgetView.builder()
                .currency(Currency.Strk)
                .remaining(BigDecimal.valueOf(100L))
                .build()))
            .build());
    OnlyDustException onlyDustException = null;
    try {
      rewardService.requestPayment(projectLeadId, requestRewardCommand);
    } catch (OnlyDustException e) {
      onlyDustException = e;
    }

    // Then
    Assertions.assertNotNull(onlyDustException);
    Assertions.assertEquals(403, onlyDustException.getStatus());
    Assertions.assertEquals("Amount must be greater than 0", onlyDustException.getMessage());
    verify(indexerPort, never()).indexUser(requestRewardCommand.getRecipientId());
  }


  @Test
  void should_throw_a_invalid_input_exception_when_there_is_no_budget_of_such_currency() {
    // Given
    final RewardServicePort rewardServicePort = mock(RewardServicePort.class);
    final PermissionService permissionService = mock(PermissionService.class);
    final ProjectStoragePort projectStoragePort = mock(ProjectStoragePort.class);
    final IndexerPort indexerPort = mock(IndexerPort.class);
    final UserStoragePort userStoragePort = mock(UserStoragePort.class);

    final RewardService rewardService =
        new RewardService(rewardServicePort, projectStoragePort, permissionService, indexerPort,
            userStoragePort);

    final UUID projectLeadId = UUID.randomUUID();
    final RequestRewardCommand requestRewardCommand =
        RequestRewardCommand.builder().projectId(UUID.randomUUID())
            .amount(BigDecimal.valueOf(10L))
            .currency(Currency.Strk)
            .build();

    // When
    when(permissionService.isUserProjectLead(requestRewardCommand.getProjectId(), projectLeadId))
        .thenReturn(true);
    when(projectStoragePort.findBudgets(requestRewardCommand.getProjectId()))
        .thenReturn(ProjectBudgetsView.builder()
            .budgets(List.of())
            .build());
    OnlyDustException onlyDustException = null;
    try {
      rewardService.requestPayment(projectLeadId, requestRewardCommand);
    } catch (OnlyDustException e) {
      onlyDustException = e;
    }

    // Then
    Assertions.assertNotNull(onlyDustException);
    Assertions.assertEquals(400, onlyDustException.getStatus());
    Assertions.assertEquals(("Not enough budget of currency Strk for project %s to request a reward with an " +
            "amount of 10").formatted(requestRewardCommand.getProjectId()),
        onlyDustException.getMessage());
    verify(indexerPort, never()).indexUser(requestRewardCommand.getRecipientId());
  }

  @Test
  void should_throw_a_invalid_input_exception_when_there_is_not_enough_budget_of_such_currency() {
    // Given
    final RewardServicePort rewardServicePort = mock(RewardServicePort.class);
    final PermissionService permissionService = mock(PermissionService.class);
    final ProjectStoragePort projectStoragePort = mock(ProjectStoragePort.class);
    final IndexerPort indexerPort = mock(IndexerPort.class);
    final UserStoragePort userStoragePort = mock(UserStoragePort.class);

    final RewardService rewardService =
        new RewardService(rewardServicePort, projectStoragePort, permissionService, indexerPort,
            userStoragePort);

    final UUID projectLeadId = UUID.randomUUID();
    final RequestRewardCommand requestRewardCommand =
        RequestRewardCommand.builder().projectId(UUID.randomUUID())
            .amount(BigDecimal.valueOf(10L))
            .currency(Currency.Strk)
            .build();

    // When
    when(permissionService.isUserProjectLead(requestRewardCommand.getProjectId(), projectLeadId))
        .thenReturn(true);
    when(projectStoragePort.findBudgets(requestRewardCommand.getProjectId()))
        .thenReturn(ProjectBudgetsView.builder()
            .budgets(List.of(BudgetView.builder()
                .currency(Currency.Strk)
                .remaining(BigDecimal.valueOf(9L))
                .build()))
            .build());
    OnlyDustException onlyDustException = null;
    try {
      rewardService.requestPayment(projectLeadId, requestRewardCommand);
    } catch (OnlyDustException e) {
      onlyDustException = e;
    }

    // Then
    Assertions.assertNotNull(onlyDustException);
    Assertions.assertEquals(400, onlyDustException.getStatus());
    Assertions.assertEquals(("Not enough budget of currency Strk for project %s to request a reward with an " +
            "amount of 10").formatted(requestRewardCommand.getProjectId()),
        onlyDustException.getMessage());
    verify(indexerPort, never()).indexUser(requestRewardCommand.getRecipientId());
  }

  @Test
  void should_cancel_reward_given_a_project_lead() {
    // Given
    final RewardServicePort rewardServicePort = mock(RewardServicePort.class);
    final PermissionService permissionService = mock(PermissionService.class);
    final ProjectStoragePort projectStoragePort = mock(ProjectStoragePort.class);
    final IndexerPort indexerPort = mock(IndexerPort.class);
    final UserStoragePort userStoragePort = mock(UserStoragePort.class);

    final RewardService rewardService =
        new RewardService(rewardServicePort, projectStoragePort, permissionService, indexerPort,
            userStoragePort);

    final UUID projectLeadId = UUID.randomUUID();
    final UUID projectId = UUID.randomUUID();
    final var rewardId = UUID.randomUUID();

    // When
    when(permissionService.isUserProjectLead(projectId, projectLeadId))
        .thenReturn(true);
    rewardService.cancelPayment(projectLeadId, projectId, rewardId);

    // Then
    verify(rewardServicePort).cancelPayment(rewardId);
  }

  @Test
  void should_throw_a_forbidden_exception_when_cancelling_reward_given_not_a_project_lead() {
    // Given
    final RewardServicePort rewardServicePort = mock(RewardServicePort.class);
    final PermissionService permissionService = mock(PermissionService.class);
    final ProjectStoragePort projectStoragePort = mock(ProjectStoragePort.class);
    final IndexerPort indexerPort = mock(IndexerPort.class);
    final UserStoragePort userStoragePort = mock(UserStoragePort.class);

    final RewardService rewardService =
        new RewardService(rewardServicePort, projectStoragePort, permissionService, indexerPort,
            userStoragePort);

    final UUID projectLeadId = UUID.randomUUID();
    final UUID projectId = UUID.randomUUID();
    final var rewardId = UUID.randomUUID();

    // When
    when(permissionService.isUserProjectLead(projectId, projectLeadId))
        .thenReturn(false);
    OnlyDustException onlyDustException = null;
    try {
      rewardService.cancelPayment(projectLeadId, projectId, rewardId);
    } catch (OnlyDustException e) {
      onlyDustException = e;
    }

    // Then
    Assertions.assertNotNull(onlyDustException);
    Assertions.assertEquals(403, onlyDustException.getStatus());
    Assertions.assertEquals("User must be project lead to cancel a reward", onlyDustException.getMessage());
  }

  @Test
  void should_mark_invoice_as_received() {
    // Given
    final RewardServicePort rewardServicePort = mock(RewardServicePort.class);
    final PermissionService permissionService = mock(PermissionService.class);
    final ProjectStoragePort projectStoragePort = mock(ProjectStoragePort.class);
    final IndexerPort indexerPort = mock(IndexerPort.class);
    final UserStoragePort userStoragePort = mock(UserStoragePort.class);

    final RewardService rewardService =
        new RewardService(rewardServicePort, projectStoragePort, permissionService, indexerPort,
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
