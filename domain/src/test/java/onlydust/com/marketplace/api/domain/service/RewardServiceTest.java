package onlydust.com.marketplace.api.domain.service;

import onlydust.com.marketplace.api.domain.exception.OnlyDustException;
import onlydust.com.marketplace.api.domain.model.Currency;
import onlydust.com.marketplace.api.domain.model.RequestRewardCommand;
import onlydust.com.marketplace.api.domain.port.output.RewardStoragePort;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RewardServiceTest {

    @Test
    void should_request_reward_given_a_project_lead() {
        // Given
        final RewardStoragePort<DummyAuthentication> rewardStoragePort = mock(RewardStoragePort.class);
        final PermissionService permissionService = mock(PermissionService.class);
        final RewardService<DummyAuthentication> rewardService =
                new RewardService<>(rewardStoragePort, permissionService);
        final DummyAuthentication authentication = new DummyAuthentication();
        final UUID projectLeadId = UUID.randomUUID();
        final RequestRewardCommand requestRewardCommand =
                RequestRewardCommand.builder()
                        .projectId(UUID.randomUUID())
                        .amount(BigDecimal.valueOf(10L))
                        .currency(Currency.Stark)
                        .build();
        final var newRewardId = UUID.randomUUID();

        // When
        when(rewardStoragePort.requestPayment(authentication, requestRewardCommand))
                .thenReturn(newRewardId);
        when(permissionService.isUserProjectLead(requestRewardCommand.getProjectId(), projectLeadId))
                .thenReturn(true);
        final UUID rewardId = rewardService.requestPayment(authentication, projectLeadId,
                requestRewardCommand);

        // Then
        assertThat(rewardId).isEqualTo(newRewardId);
    }

    @Test
    void should_throw_a_forbidden_exception_given_not_a_project_lead() {
        // Given
        final RewardStoragePort<DummyAuthentication> rewardStoragePort = mock(RewardStoragePort.class);
        final PermissionService permissionService = mock(PermissionService.class);
        final RewardService<DummyAuthentication> rewardService =
                new RewardService<>(rewardStoragePort, permissionService);
        final DummyAuthentication authentication = new DummyAuthentication();
        final UUID projectLeadId = UUID.randomUUID();
        final RequestRewardCommand requestRewardCommand =
                RequestRewardCommand.builder().projectId(UUID.randomUUID())
                        .amount(BigDecimal.valueOf(10L))
                        .currency(Currency.Stark)
                        .build();

        // When
        when(permissionService.isUserProjectLead(requestRewardCommand.getProjectId(), projectLeadId))
                .thenReturn(false);
        OnlyDustException onlyDustException = null;
        try {
            rewardService.requestPayment(authentication, projectLeadId, requestRewardCommand);
        } catch (OnlyDustException e) {
            onlyDustException = e;
        }

        // Then
        Assertions.assertNotNull(onlyDustException);
        Assertions.assertEquals(403, onlyDustException.getStatus());
        Assertions.assertEquals("User must be project lead to request a reward", onlyDustException.getMessage());
    }

    private static class DummyAuthentication {

    }

}
