package onlydust.com.marketplace.accounting.domain.observer;

import onlydust.com.marketplace.accounting.domain.model.*;
import onlydust.com.marketplace.accounting.domain.model.accountbook.AccountBook.AccountId;
import onlydust.com.marketplace.accounting.domain.model.accountbook.AccountBookAggregate;
import onlydust.com.marketplace.accounting.domain.port.out.RewardStatusStorage;
import onlydust.com.marketplace.accounting.domain.service.RewardStatusService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class RewardStatusServiceTest {

    private RewardStatusStorage rewardStatusStorage;

    @Nested
    class OnRewardCreated {
        RewardStatusService rewardStatusService;
        final SponsorAccount.Id sponsorAccountId = SponsorAccount.Id.random();
        final ProjectId projectId1 = ProjectId.random();
        RewardId rewardId = RewardId.random();
        AccountBookAggregate accountBook;

        @BeforeEach
        void setUp() {
            final var sponsorAccountAccountId = AccountId.of(sponsorAccountId);
            final var projectAccountId = AccountId.of(projectId1);
            final var rewardAccountId = AccountId.of(rewardId);

            accountBook = AccountBookAggregate.fromEvents(
                    new AccountBookAggregate.MintEvent(sponsorAccountAccountId, PositiveAmount.of(100L)),
                    new AccountBookAggregate.TransferEvent(sponsorAccountAccountId, projectAccountId, PositiveAmount.of(100L)),
                    new AccountBookAggregate.TransferEvent(projectAccountId, rewardAccountId, PositiveAmount.of(20L))
            );

            rewardStatusStorage = mock(RewardStatusStorage.class);
            rewardStatusService = new RewardStatusService(rewardStatusStorage);
        }

        @Test
        public void should_create_status() {
            // Given
            final var rewardStatus = new RewardStatus(rewardId)
                    .sponsorHasEnoughFund(true)
                    .unlockDate(ZonedDateTime.now())
                    .paymentRequestedAt(null)
                    .paidAt(null)
                    .networks(Set.of(Network.ETHEREUM, Network.OPTIMISM));

            // When
            rewardStatusService.onRewardCreated(rewardStatus);

            // Then
            verify(rewardStatusStorage).save(rewardStatus);
        }
    }

    @Nested
    class OnRewardCancelled {
        RewardStatusService rewardStatusService;
        RewardId rewardId = RewardId.random();

        @BeforeEach
        void setUp() {
            rewardStatusStorage = mock(RewardStatusStorage.class);
            rewardStatusService = new RewardStatusService(rewardStatusStorage);
        }

        @Test
        public void should_delete_status() {
            // When
            rewardStatusService.onRewardCancelled(rewardId);

            // Then
            verify(rewardStatusStorage).delete(rewardId);
        }
    }

    @Nested
    class OnSponsorAccountBalanceChanged {
        RewardStatusService rewardStatusService;
        RewardId rewardId = RewardId.random();
        RewardId rewardId2 = RewardId.random();
        SponsorAccountStatement sponsorAccountStatement;
        SponsorAccount sponsorAccount;

        @BeforeEach
        void setUp() {
            rewardStatusStorage = mock(RewardStatusStorage.class);
            sponsorAccount = mock(SponsorAccount.class);
            sponsorAccountStatement = mock(SponsorAccountStatement.class);
            rewardStatusService = new RewardStatusService(rewardStatusStorage);
            when(sponsorAccountStatement.account()).thenReturn(sponsorAccount);
        }

        @Test
        public void should_update_reward_statuses() {
            // Given
            when(sponsorAccountStatement.awaitingPayments()).thenReturn(Map.of(
                    rewardId, PositiveAmount.of(100L),
                    rewardId2, PositiveAmount.of(2000L)
            ));
            when(sponsorAccount.balance()).thenReturn(PositiveAmount.of(100L));
            when(rewardStatusStorage.get(any())).then(invocation -> {
                final var rewardId = invocation.getArgument(0, RewardId.class);
                return Optional.of(new RewardStatus(rewardId));
            });

            // When
            rewardStatusService.onSponsorAccountBalanceChanged(sponsorAccountStatement);

            // Then
            final var rewardStatusCaptor = ArgumentCaptor.forClass(RewardStatus.class);
            verify(rewardStatusStorage, times(2)).save(rewardStatusCaptor.capture());
            final var rewardStatuses = rewardStatusCaptor.getAllValues();
            assertThat(rewardStatuses).hasSize(2);
            assertThat(rewardStatuses.stream().filter(r -> r.rewardId().equals(rewardId)).findFirst().orElseThrow().sponsorHasEnoughFund()).isTrue();
            assertThat(rewardStatuses.stream().filter(r -> r.rewardId().equals(rewardId2)).findFirst().orElseThrow().sponsorHasEnoughFund()).isFalse();
        }
    }
}
