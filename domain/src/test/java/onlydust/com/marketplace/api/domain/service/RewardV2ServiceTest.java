package onlydust.com.marketplace.api.domain.service;

import com.github.javafaker.Faker;
import onlydust.com.marketplace.api.domain.model.Currency;
import onlydust.com.marketplace.api.domain.model.RequestRewardCommand;
import onlydust.com.marketplace.api.domain.model.Reward;
import onlydust.com.marketplace.api.domain.port.output.AccountingServicePort;
import onlydust.com.marketplace.api.domain.port.output.IndexerPort;
import onlydust.com.marketplace.api.domain.port.output.RewardServicePort;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

public class RewardV2ServiceTest {
    final RewardServicePort rewardServicePort = mock(RewardServicePort.class);
    final PermissionService permissionService = mock(PermissionService.class);
    final IndexerPort indexerPort = mock(IndexerPort.class);
    final AccountingServicePort accountingServicePort = mock(AccountingServicePort.class);
    final RewardV2Service rewardService = new RewardV2Service(
            rewardServicePort,
            permissionService,
            indexerPort,
            accountingServicePort
    );
    final UUID projectLeadId = UUID.randomUUID();
    final UUID projectId = UUID.randomUUID();
    final Faker faker = new Faker();
    final Long recipientId = faker.number().randomNumber(5, true);
    final UUID rewardId = UUID.randomUUID();

    @BeforeEach
    void setup() {
        reset(rewardServicePort, permissionService, indexerPort, accountingServicePort);
    }

    @Nested
    class GivenAProject {
        @Test
        void should_create_reward() {
            // Given
            when(permissionService.isUserProjectLead(projectId, projectLeadId)).thenReturn(true);
            final var command = RequestRewardCommand.builder()
                    .projectId(projectId)
                    .recipientId(recipientId)
                    .amount(BigDecimal.TEN)
                    .currency(Currency.Usdc)
                    .build();
            when(rewardServicePort.create(projectLeadId, command)).thenReturn(rewardId);

            // When
            final var createdRewardId = rewardService.requestPayment(projectLeadId, command);

            // Then
            assertThat(createdRewardId).isEqualTo(rewardId);
            verify(indexerPort).indexUser(recipientId);
            verify(accountingServicePort).createReward(projectId, rewardId, BigDecimal.TEN, "USDC");
        }

        @Test
        void should_prevent_non_project_lead_to_create_reward() {
            // Given
            when(permissionService.isUserProjectLead(projectId, projectLeadId)).thenReturn(false);
            final var command = RequestRewardCommand.builder()
                    .projectId(projectId)
                    .recipientId(recipientId)
                    .amount(BigDecimal.TEN)
                    .currency(Currency.Usdc)
                    .build();

            // When
            assertThatThrownBy(() -> rewardService.requestPayment(projectLeadId, command))
                    // Then
                    .isInstanceOf(OnlyDustException.class)
                    .hasMessage("User must be project lead to request a reward");
        }

        @Test
        void should_prevent_creating_reward_with_negative_amount() {
            // Given
            when(permissionService.isUserProjectLead(projectId, projectLeadId)).thenReturn(true);
            final var command = RequestRewardCommand.builder()
                    .projectId(projectId)
                    .recipientId(recipientId)
                    .amount(BigDecimal.TEN.negate())
                    .currency(Currency.Usdc)
                    .build();

            // When
            assertThatThrownBy(() -> rewardService.requestPayment(projectLeadId, command))
                    // Then
                    .isInstanceOf(OnlyDustException.class)
                    .hasMessage("Amount must be greater than 0");
        }

        @Test
        void should_prevent_creating_reward_with_zero_amount() {
            // Given
            when(permissionService.isUserProjectLead(projectId, projectLeadId)).thenReturn(true);
            final var command = RequestRewardCommand.builder()
                    .projectId(projectId)
                    .recipientId(recipientId)
                    .amount(BigDecimal.ZERO)
                    .currency(Currency.Usdc)
                    .build();

            // When
            assertThatThrownBy(() -> rewardService.requestPayment(projectLeadId, command))
                    // Then
                    .isInstanceOf(OnlyDustException.class)
                    .hasMessage("Amount must be greater than 0");
        }

        @Test
        void should_prevent_cancelling_a_non_existing_reward() {
            // Given
            when(permissionService.isUserProjectLead(projectId, projectLeadId)).thenReturn(true);
            when(rewardServicePort.get(rewardId)).thenReturn(Optional.empty());

            // When
            assertThatThrownBy(() -> rewardService.cancelPayment(projectLeadId, projectId, rewardId))
                    // Then
                    .isInstanceOf(OnlyDustException.class)
                    .hasMessage("Reward %s not found".formatted(rewardId));
        }
    }

    @Nested
    class GivenAProjectWithAReward {
        final Reward reward = new Reward(
                rewardId,
                projectId,
                projectLeadId,
                recipientId,
                BigDecimal.TEN,
                Currency.Usdc,
                new Date(),
                null,
                List.of()
        );

        @BeforeEach
        void setup() {
            when(rewardServicePort.get(rewardId)).thenReturn(Optional.of(reward));
        }

        @Test
        void should_cancel_reward() {
            // Given
            when(permissionService.isUserProjectLead(projectId, projectLeadId)).thenReturn(true);

            // When
            rewardService.cancelPayment(projectLeadId, projectId, rewardId);

            // Then
            verify(rewardServicePort).cancel(rewardId);
            verify(accountingServicePort).cancelReward(projectId, rewardId, BigDecimal.TEN, "USDC");
        }

        @Test
        void should_prevent_non_project_lead_to_cancel_reward() {
            // Given
            when(permissionService.isUserProjectLead(projectId, projectLeadId)).thenReturn(false);

            // When
            assertThatThrownBy(() -> rewardService.cancelPayment(projectLeadId, projectId, rewardId))
                    // Then
                    .isInstanceOf(OnlyDustException.class)
                    .hasMessage("User must be project lead to cancel a reward");
        }
    }
}
