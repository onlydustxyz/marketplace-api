package onlydust.com.marketplace.accounting.domain.observer;

import com.github.javafaker.Faker;
import onlydust.com.marketplace.accounting.domain.model.*;
import onlydust.com.marketplace.accounting.domain.port.out.CurrencyStorage;
import onlydust.com.marketplace.accounting.domain.port.out.QuoteStorage;
import onlydust.com.marketplace.accounting.domain.port.out.RewardStatusStorage;
import onlydust.com.marketplace.accounting.domain.port.out.RewardUsdEquivalentStorage;
import onlydust.com.marketplace.accounting.domain.service.AccountBookFacade;
import onlydust.com.marketplace.accounting.domain.service.RewardStatusService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static onlydust.com.marketplace.accounting.domain.stubs.Currencies.ETH;
import static onlydust.com.marketplace.accounting.domain.stubs.Currencies.USD;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class RewardStatusServiceTest {
    private RewardStatusStorage rewardStatusStorage;
    private AccountBookFacade accountBookFacade;
    RewardUsdEquivalentStorage rewardUsdEquivalentStorage;
    QuoteStorage quoteStorage;
    CurrencyStorage currencyStorage;
    RewardStatusService rewardStatusService;
    final Faker faker = new Faker();
    final Currency currency = ETH;
    final Currency usd = USD;
    final BigDecimal rewardAmount = BigDecimal.valueOf(faker.number().randomNumber(3, true));
    final RewardUsdEquivalent rewardUsdEquivalent = mock(RewardUsdEquivalent.class);
    final ZonedDateTime equivalenceSealingDate = ZonedDateTime.now().minusDays(1);
    final BigDecimal price = BigDecimal.valueOf(123.25);

    @BeforeEach
    void setUp() {
        rewardStatusStorage = mock(RewardStatusStorage.class);
        accountBookFacade = mock(AccountBookFacade.class);
        rewardUsdEquivalentStorage = mock(RewardUsdEquivalentStorage.class);
        quoteStorage = mock(QuoteStorage.class);
        currencyStorage = mock(CurrencyStorage.class);
        when(currencyStorage.findByCode(usd.code())).thenReturn(Optional.of(usd));
        rewardStatusService = new RewardStatusService(rewardStatusStorage, rewardUsdEquivalentStorage, quoteStorage, currencyStorage);

        when(rewardStatusStorage.get(any(RewardId.class))).then(invocation -> {
            final var rewardId = invocation.getArgument(0, RewardId.class);
            return Optional.of(new RewardStatusData(rewardId));
        });
        when(rewardUsdEquivalentStorage.get(any())).thenReturn(Optional.of(rewardUsdEquivalent));
        when(rewardUsdEquivalent.rewardAmount()).thenReturn(rewardAmount);
        when(rewardUsdEquivalent.rewardCurrencyId()).thenReturn(currency.id());
        when(rewardUsdEquivalent.equivalenceSealingDate()).thenReturn(Optional.of(equivalenceSealingDate));
        when(quoteStorage.nearest(currency.id(), usd.id(), equivalenceSealingDate))
                .thenReturn(Optional.of(new Quote(currency.id(), usd.id(), price, equivalenceSealingDate.minusSeconds(30).toInstant())));
    }

    @Test
    void should_create_an_uptodate_reward_status() {
        // Given
        final var rewardId = RewardId.random();
        final var unlockDate = Instant.now();
        final var networks = Set.of(Network.ETHEREUM, Network.OPTIMISM);

        when(accountBookFacade.isFunded(rewardId)).thenReturn(true);
        when(accountBookFacade.unlockDateOf(rewardId)).thenReturn(Optional.of(unlockDate));
        when(accountBookFacade.networksOf(rewardId)).thenReturn(networks);

        // When
        rewardStatusService.create(accountBookFacade, rewardId);

        // Then
        final var capturedRewardStatus = ArgumentCaptor.forClass(RewardStatusData.class);
        verify(rewardStatusStorage).persist(capturedRewardStatus.capture());
        final var savedRewardStatus = capturedRewardStatus.getValue();
        assertThat(savedRewardStatus.rewardId()).isEqualTo(rewardId);
        assertThat(savedRewardStatus.sponsorHasEnoughFund()).isTrue();
        assertThat(savedRewardStatus.unlockDate()).isPresent();
        assertThat(savedRewardStatus.unlockDate().get()).isEqualToIgnoringNanos(unlockDate.atZone(ZoneOffset.UTC));
        assertThat(savedRewardStatus.networks()).isEqualTo(networks);
        assertThat(savedRewardStatus.usdAmount()).isPresent();
        assertThat(savedRewardStatus.usdAmount().get().conversionRate()).isEqualTo(price);
    }


    @Test
    void should_delete_a_status() {
        // Given
        final var rewardId = RewardId.random();

        // When
        rewardStatusService.delete(rewardId);

        // Then
        verify(rewardStatusStorage).delete(rewardId);
    }

    @Nested
    class RefreshRewardsUsdEquivalent {
        final RewardId rewardId = RewardId.random();

        @Nested
        class GivenAReward {
            final RewardUsdEquivalent rewardUsdEquivalent = mock(RewardUsdEquivalent.class);
            final BigDecimal rewardAmount = BigDecimal.valueOf(faker.number().randomNumber(3, true));

            @BeforeEach
            void setup() {
                when(rewardStatusStorage.get(List.of(rewardId))).thenReturn(List.of(new RewardStatusData(rewardId)));
                when(rewardUsdEquivalentStorage.get(rewardId)).thenReturn(Optional.of(rewardUsdEquivalent));
                when(rewardUsdEquivalent.rewardAmount()).thenReturn(rewardAmount);
                when(rewardUsdEquivalent.rewardCurrencyId()).thenReturn(currency.id());
            }

            @Test
            void should_reset_usd_equivalent_if_no_equivalence_date_found() {
                // Given
                when(rewardUsdEquivalent.equivalenceSealingDate()).thenReturn(Optional.empty());

                // When
                rewardStatusService.refreshRewardsUsdEquivalentOf(rewardId);

                // Then
                final var rewardIdCaptor = ArgumentCaptor.forClass(RewardId.class);
                final var rewardUsdAmountCaptor = ArgumentCaptor.forClass(ConvertedAmount.class);
                verify(rewardStatusStorage).updateUsdAmount(rewardIdCaptor.capture(), rewardUsdAmountCaptor.capture());
                assertThat(rewardIdCaptor.getValue()).isEqualTo(rewardId);
                assertThat(rewardUsdAmountCaptor.getValue()).isNull();
            }

            @Test
            void should_update_usd_equivalent_if_equivalence_date_found() {
                // Given
                final var equivalenceSealingDate = ZonedDateTime.now().minusDays(1);
                final var price = BigDecimal.valueOf(123.25);
                when(rewardUsdEquivalent.equivalenceSealingDate()).thenReturn(Optional.of(equivalenceSealingDate));
                when(quoteStorage.nearest(currency.id(), usd.id(), equivalenceSealingDate))
                        .thenReturn(Optional.of(new Quote(currency.id(), usd.id(), price, equivalenceSealingDate.minusSeconds(30).toInstant())));

                // When
                rewardStatusService.refreshRewardsUsdEquivalentOf(rewardId);

                // Then
                final var rewardIdCaptor = ArgumentCaptor.forClass(RewardId.class);
                final var rewardUsdAmountCaptor = ArgumentCaptor.forClass(ConvertedAmount.class);
                verify(rewardStatusStorage).updateUsdAmount(rewardIdCaptor.capture(), rewardUsdAmountCaptor.capture());
                assertThat(rewardIdCaptor.getValue()).isEqualTo(rewardId);
                assertThat(rewardUsdAmountCaptor.getValue()).isNotNull();
                assertThat(rewardUsdAmountCaptor.getValue().convertedAmount().getValue()).isEqualTo(price.multiply(rewardAmount));
                assertThat(rewardUsdAmountCaptor.getValue().conversionRate()).isEqualTo(price);
            }
        }

        @Test
        void should_refresh_usd_equivalent_for_all_rewards_not_paid() {
            // Given
            final var rewardId1 = RewardId.random();
            final var rewardId2 = RewardId.random();
            when(rewardStatusStorage.notRequested()).thenReturn(List.of(
                    new RewardStatusData(rewardId1),
                    new RewardStatusData(rewardId2)
            ));

            // When
            rewardStatusService.refreshRewardsUsdEquivalents();

            // Then
            final var rewardIdCaptor = ArgumentCaptor.forClass(RewardId.class);
            final var rewardUsdAmountCaptor = ArgumentCaptor.forClass(ConvertedAmount.class);
            verify(rewardStatusStorage, times(2)).updateUsdAmount(rewardIdCaptor.capture(), rewardUsdAmountCaptor.capture());
            assertThat(rewardIdCaptor.getAllValues()).hasSize(2);
            assertThat(rewardUsdAmountCaptor.getAllValues()).allMatch(r -> r != null && r.convertedAmount().getValue().compareTo(BigDecimal.ZERO) > 0);
        }
    }
}
