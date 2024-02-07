package onlydust.com.marketplace.accounting.domain.observer;

import onlydust.com.marketplace.accounting.domain.model.*;
import onlydust.com.marketplace.accounting.domain.model.accountbook.AccountBook.AccountId;
import onlydust.com.marketplace.accounting.domain.model.accountbook.AccountBookAggregate;
import onlydust.com.marketplace.accounting.domain.port.out.RewardStatusStorage;
import onlydust.com.marketplace.accounting.domain.service.RewardStatusService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;
import java.util.Set;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

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
}
