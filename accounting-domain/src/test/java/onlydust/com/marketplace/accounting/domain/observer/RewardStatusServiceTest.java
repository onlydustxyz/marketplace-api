package onlydust.com.marketplace.accounting.domain.observer;

import onlydust.com.marketplace.accounting.domain.model.*;
import onlydust.com.marketplace.accounting.domain.model.accountbook.AccountBook.AccountId;
import onlydust.com.marketplace.accounting.domain.model.accountbook.AccountBookAggregate;
import onlydust.com.marketplace.accounting.domain.port.out.RewardStatusStorage;
import onlydust.com.marketplace.accounting.domain.service.AccountBookFacade;
import onlydust.com.marketplace.accounting.domain.service.RewardStatusService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class RewardStatusServiceTest {
    private RewardStatusStorage rewardStatusStorage;
    private AccountBookFacade accountBookFacade;
    RewardStatusService rewardStatusService;

    @BeforeEach
    void setUp() {
        rewardStatusStorage = mock(RewardStatusStorage.class);
        accountBookFacade = mock(AccountBookFacade.class);
        rewardStatusService = new RewardStatusService(rewardStatusStorage);
    }

    @Nested
    class OnRewardCreated {
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
        }

        @Test
        public void should_create_status() {
            // Given
            final var rewardStatus = new RewardStatus(rewardId)
                    .sponsorHasEnoughFund(true)
                    .unlockDate(ZonedDateTime.now().toInstant().atZone(ZoneOffset.UTC))
                    .paymentRequestedAt(null)
                    .paidAt(null)
                    .withAdditionalNetworks(Set.of(Network.ETHEREUM, Network.OPTIMISM));

            when(accountBookFacade.isFunded(rewardId)).thenReturn(true);
            when(accountBookFacade.unlockDateOf(rewardId)).thenReturn(rewardStatus.unlockDate().map(ZonedDateTime::toInstant));
            when(accountBookFacade.networksOf(rewardId)).thenReturn(rewardStatus.networks());

            // When
            rewardStatusService.onRewardCreated(rewardId, accountBookFacade);

            // Then
            verify(rewardStatusStorage).save(rewardStatus);
        }
    }

    @Nested
    class OnRewardCancelled {
        RewardId rewardId = RewardId.random();

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
        RewardId rewardId1 = RewardId.random();
        RewardId rewardId2 = RewardId.random();
        SponsorAccountStatement sponsorAccountStatement;
        SponsorAccount sponsorAccount;

        @BeforeEach
        void setUp() {
            sponsorAccount = mock(SponsorAccount.class);
            sponsorAccountStatement = mock(SponsorAccountStatement.class);
            when(sponsorAccountStatement.account()).thenReturn(sponsorAccount);
        }

        @Test
        public void should_update_reward_status_data() {
            // Given
            final var unlockDate = ZonedDateTime.now().plusDays(1);
            when(sponsorAccountStatement.awaitingPayments()).thenReturn(Map.of(
                    rewardId1, PositiveAmount.of(100L),
                    rewardId2, PositiveAmount.of(2000L)
            ));
            when(sponsorAccountStatement.accountBookFacade()).thenReturn(accountBookFacade);
            when(accountBookFacade.isFunded(rewardId1)).thenReturn(true);
            when(accountBookFacade.isFunded(rewardId2)).thenReturn(false);
            when(accountBookFacade.unlockDateOf(any())).thenReturn(Optional.of(unlockDate.toInstant()));
            when(accountBookFacade.networksOf(any())).thenReturn(Set.of(Network.ETHEREUM, Network.OPTIMISM));

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
            assertThat(rewardStatuses.stream().filter(r -> r.rewardId().equals(rewardId1)).findFirst().orElseThrow().sponsorHasEnoughFund()).isTrue();
            assertThat(rewardStatuses.stream().filter(r -> r.rewardId().equals(rewardId2)).findFirst().orElseThrow().sponsorHasEnoughFund()).isFalse();
            assertThat(rewardStatuses).allMatch(r -> r.networks().containsAll(Set.of(Network.ETHEREUM, Network.OPTIMISM)));
            assertThat(rewardStatuses).allMatch(r -> r.unlockDate().orElseThrow().toInstant().equals(unlockDate.toInstant()));
        }
    }

    @Nested
    class OnSponsorAccountUpdated {
        RewardId rewardId1 = RewardId.random();
        RewardId rewardId2 = RewardId.random();
        SponsorAccountStatement sponsorAccountStatement;
        SponsorAccount sponsorAccount;

        @BeforeEach
        void setUp() {
            sponsorAccount = mock(SponsorAccount.class);
            sponsorAccountStatement = mock(SponsorAccountStatement.class);
            when(sponsorAccountStatement.account()).thenReturn(sponsorAccount);
        }

        @Test
        public void should_update_reward_status_data() {
            // Given
            final var unlockDate = ZonedDateTime.now().plusDays(1);
            when(sponsorAccountStatement.awaitingPayments()).thenReturn(Map.of(
                    rewardId1, PositiveAmount.of(100L),
                    rewardId2, PositiveAmount.of(2000L)
            ));
            when(sponsorAccountStatement.accountBookFacade()).thenReturn(accountBookFacade);
            when(accountBookFacade.isFunded(rewardId1)).thenReturn(false);
            when(accountBookFacade.isFunded(rewardId2)).thenReturn(true);
            when(accountBookFacade.unlockDateOf(any())).thenReturn(Optional.of(unlockDate.plusDays(1).toInstant()));
            when(accountBookFacade.networksOf(any())).thenReturn(Set.of(Network.APTOS, Network.OPTIMISM));

            when(rewardStatusStorage.get(any())).then(invocation -> {
                final var rewardId = invocation.getArgument(0, RewardId.class);
                return Optional.of(new RewardStatus(rewardId)
                        .sponsorHasEnoughFund(rewardId.equals(rewardId1))
                        .unlockDate(unlockDate)
                        .withAdditionalNetworks(Set.of(Network.ETHEREUM, Network.OPTIMISM)));
            });

            // When
            rewardStatusService.onSponsorAccountBalanceChanged(sponsorAccountStatement);

            // Then
            final var rewardStatusCaptor = ArgumentCaptor.forClass(RewardStatus.class);
            verify(rewardStatusStorage, times(2)).save(rewardStatusCaptor.capture());
            final var rewardStatuses = rewardStatusCaptor.getAllValues();
            assertThat(rewardStatuses).hasSize(2);
            assertThat(rewardStatuses.stream().filter(r -> r.rewardId().equals(rewardId1)).findFirst().orElseThrow().sponsorHasEnoughFund()).isFalse();
            assertThat(rewardStatuses.stream().filter(r -> r.rewardId().equals(rewardId2)).findFirst().orElseThrow().sponsorHasEnoughFund()).isTrue();
            assertThat(rewardStatuses).allMatch(r -> r.networks().containsAll(Set.of(Network.APTOS, Network.OPTIMISM)));
            assertThat(rewardStatuses).allMatch(r -> r.unlockDate().orElseThrow().toInstant().equals(unlockDate.plusDays(1).toInstant()));
        }
    }
}
