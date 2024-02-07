package onlydust.com.marketplace.api.infrastructure.accounting;

import com.github.javafaker.Faker;
import onlydust.com.marketplace.accounting.domain.model.Currency;
import onlydust.com.marketplace.accounting.domain.model.RewardId;
import onlydust.com.marketplace.accounting.domain.model.RewardStatus;
import onlydust.com.marketplace.accounting.domain.port.in.CurrencyFacadePort;
import onlydust.com.marketplace.accounting.domain.port.out.RewardStatusStorage;
import onlydust.com.marketplace.api.domain.model.Reward;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class AccountingObserverAdapterTest {
    final RewardStatusStorage rewardStatusStorage = mock(RewardStatusStorage.class);
    final CurrencyFacadePort currencyFacadePort = mock(CurrencyFacadePort.class);
    final AccountingObserverAdapter accountingObserverAdapter = new AccountingObserverAdapter(rewardStatusStorage, currencyFacadePort);
    final Faker faker = new Faker();
    final Currency ETH = Currency.crypto("Ether", Currency.Code.of("ETH"), 18);

    @BeforeEach
    void setup() {
        reset(rewardStatusStorage, currencyFacadePort);
    }

    @Nested
    class GivenNoReward {
        @Test
        void on_reward_created() {
            // Given
            final var reward = fakeReward();
            when(currencyFacadePort.listCurrencies()).thenReturn(List.of(ETH));

            // When
            accountingObserverAdapter.onRewardCreated(reward);

            // Then
            final var rewardStatusCaptor = ArgumentCaptor.forClass(RewardStatus.class);
            verify(rewardStatusStorage).save(rewardStatusCaptor.capture());
            final var rewardStatus = rewardStatusCaptor.getValue();
            assertThat(rewardStatus.rewardId().value()).isEqualTo(reward.id());
            assertThat(rewardStatus.rewardCurrency().code().toString()).isEqualTo(reward.currency().toString().toUpperCase());
        }
    }

    @Nested
    class GivenAReward {

        final Reward reward = fakeReward();
        final RewardStatus rewardStatus = new RewardStatus(RewardId.of(reward.id())).rewardCurrency(ETH);

        @BeforeEach
        void setup() {
            when(rewardStatusStorage.get(RewardId.of(reward.id()))).thenReturn(java.util.Optional.of(rewardStatus));
        }

        @Test
        void on_reward_created_with_unsupported_currency() {
            // Given
            when(currencyFacadePort.listCurrencies()).thenReturn(List.of());

            // When
            assertThatThrownBy(() -> accountingObserverAdapter.onRewardCreated(reward))
                    // Then
                    .isInstanceOf(OnlyDustException.class)
                    .hasMessage("Unsupported currency Eth");
        }

        @Test
        void on_reward_cancelled() {
            // When
            accountingObserverAdapter.onRewardCancelled(reward.id());

            // Then
            verify(rewardStatusStorage).delete(RewardId.of(reward.id()));
        }

        @Test
        void on_payment_requested() {
            // When
            accountingObserverAdapter.onPaymentRequested(reward.id());

            // Then
            final var rewardStatusCaptor = ArgumentCaptor.forClass(RewardStatus.class);
            verify(rewardStatusStorage).save(rewardStatusCaptor.capture());
            final var rewardStatus = rewardStatusCaptor.getValue();
            assertThat(rewardStatus.paymentRequested()).isTrue();
        }
    }


    private Reward fakeReward() {
        return new Reward(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                faker.number().randomNumber(5, true),
                BigDecimal.valueOf(faker.number().randomNumber(2, true)),
                onlydust.com.marketplace.api.domain.model.Currency.Eth,
                new Date(),
                null,
                List.of());
    }
}