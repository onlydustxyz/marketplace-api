package onlydust.com.marketplace.project.domain.service;

import com.github.javafaker.Faker;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import onlydust.com.marketplace.kernel.model.CurrencyView;
import onlydust.com.marketplace.kernel.port.output.IndexerPort;
import onlydust.com.marketplace.project.domain.model.RequestRewardCommand;
import onlydust.com.marketplace.project.domain.model.Reward;
import onlydust.com.marketplace.project.domain.port.output.AccountingServicePort;
import onlydust.com.marketplace.project.domain.port.output.RewardStoragePort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

public class RewardServiceTest {
    final RewardStoragePort rewardStoragePort = mock(RewardStoragePort.class);
    final PermissionService permissionService = mock(PermissionService.class);
    final IndexerPort indexerPort = mock(IndexerPort.class);
    final AccountingServicePort accountingServicePort = mock(AccountingServicePort.class);
    final RewardService rewardService = new RewardService(
            rewardStoragePort,
            permissionService,
            indexerPort,
            accountingServicePort
    );
    final UUID projectLeadId = UUID.randomUUID();
    final UUID projectId = UUID.randomUUID();
    final Faker faker = new Faker();
    final Long recipientId = faker.number().randomNumber(5, true);
    final CurrencyView.Id usdcId = CurrencyView.Id.random();

    @BeforeEach
    void setup() {
        reset(rewardStoragePort, permissionService, indexerPort, accountingServicePort);
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
                    .currencyId(usdcId)
                    .items(List.of())
                    .build();


            // When
            final var rewardId = rewardService.createReward(projectLeadId, command);

            // Then
            assertThat(rewardId).isNotNull();
            verify(indexerPort).indexUser(recipientId);

            final var reward = ArgumentCaptor.forClass(Reward.class);
            verify(rewardStoragePort).save(reward.capture());
            final var capturedReward = reward.getValue();
            assertThat(capturedReward.id()).isEqualTo(rewardId);
            assertThat(capturedReward.projectId()).isEqualTo(projectId);
            assertThat(capturedReward.requestorId()).isEqualTo(projectLeadId);
            assertThat(capturedReward.recipientId()).isEqualTo(recipientId);
            assertThat(capturedReward.amount()).isEqualTo(BigDecimal.TEN);
            assertThat(capturedReward.currencyId()).isEqualTo(usdcId);
            assertThat(capturedReward.rewardItems()).isEmpty();

            verify(accountingServicePort).createReward(projectId, rewardId, BigDecimal.TEN, usdcId);
        }

        @Test
        void should_prevent_non_project_lead_to_create_reward() {
            // Given
            when(permissionService.isUserProjectLead(projectId, projectLeadId)).thenReturn(false);
            final var command = RequestRewardCommand.builder()
                    .projectId(projectId)
                    .recipientId(recipientId)
                    .amount(BigDecimal.TEN)
                    .currencyId(usdcId)
                    .build();

            // When
            assertThatThrownBy(() -> rewardService.createReward(projectLeadId, command))
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
                    .currencyId(usdcId)
                    .build();

            // When
            assertThatThrownBy(() -> rewardService.createReward(projectLeadId, command))
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
                    .currencyId(usdcId)
                    .build();

            // When
            assertThatThrownBy(() -> rewardService.createReward(projectLeadId, command))
                    // Then
                    .isInstanceOf(OnlyDustException.class)
                    .hasMessage("Amount must be greater than 0");
        }

        @Test
        void should_prevent_cancelling_a_non_existing_reward() {
            // Given
            final var rewardId = UUID.randomUUID();
            when(permissionService.isUserProjectLead(projectId, projectLeadId)).thenReturn(true);
            when(rewardStoragePort.get(rewardId)).thenReturn(Optional.empty());

            // When
            assertThatThrownBy(() -> rewardService.cancelReward(projectLeadId, projectId, rewardId))
                    // Then
                    .isInstanceOf(OnlyDustException.class)
                    .hasMessage("Reward %s not found".formatted(rewardId));
        }
    }

    @Nested
    class GivenAProjectWithAReward {
        final UUID rewardId = UUID.randomUUID();

        final Reward reward = new Reward(
                rewardId,
                projectId,
                projectLeadId,
                recipientId,
                BigDecimal.TEN,
                usdcId,
                new Date(),
                List.of()
        );

        @BeforeEach
        void setup() {
            when(rewardStoragePort.get(rewardId)).thenReturn(Optional.of(reward));
        }

        @Test
        void should_cancel_reward() {
            // Given
            when(permissionService.isUserProjectLead(projectId, projectLeadId)).thenReturn(true);

            // When
            rewardService.cancelReward(projectLeadId, projectId, rewardId);

            // Then
            verify(rewardStoragePort).delete(rewardId);
            verify(accountingServicePort).cancelReward(rewardId, usdcId);
        }

        @Test
        void should_prevent_non_project_lead_to_cancel_reward() {
            // Given
            when(permissionService.isUserProjectLead(projectId, projectLeadId)).thenReturn(false);

            // When
            assertThatThrownBy(() -> rewardService.cancelReward(projectLeadId, projectId, rewardId))
                    // Then
                    .isInstanceOf(OnlyDustException.class)
                    .hasMessage("User must be project lead to cancel a reward");
        }
    }
}
