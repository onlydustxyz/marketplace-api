package onlydust.com.marketplace.accounting.domain.model;

import onlydust.com.marketplace.kernel.model.RewardId;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class RewardUsdEquivalentTest {
    final RewardId rewardId = RewardId.random();
    final Currency.Id currencyId = Currency.Id.random();
    final ZonedDateTime rewardCreatedAt = ZonedDateTime.now().minusDays(2);
    final ZonedDateTime kycbVerifiedAt = ZonedDateTime.now().minusDays(2);
    final ZonedDateTime currencyQuoteAvailableAt = ZonedDateTime.now().minusDays(2);
    final ZonedDateTime unlockDate = ZonedDateTime.now().minusDays(2);
    final BigDecimal amount = BigDecimal.TEN;

    @Test
    void should_not_return_date_if_kyc_not_approved() {
        // When
        final var rewardUsdEquivalent = new RewardUsdEquivalent(rewardId, rewardCreatedAt, currencyId, null, currencyQuoteAvailableAt, unlockDate, amount);

        // Then
        assertThat(rewardUsdEquivalent.equivalenceSealingDate()).isEmpty();
    }

    @Test
    void should_not_return_date_if_token_not_liquid() {
        // When
        final var rewardUsdEquivalent = new RewardUsdEquivalent(rewardId, rewardCreatedAt, currencyId, kycbVerifiedAt, null, unlockDate, amount);

        // Then
        assertThat(rewardUsdEquivalent.equivalenceSealingDate()).isEmpty();
    }

    @Test
    void should_not_return_date_if_token_is_locked() {
        // When
        final var rewardUsdEquivalent = new RewardUsdEquivalent(rewardId, rewardCreatedAt, currencyId, kycbVerifiedAt, currencyQuoteAvailableAt,
                ZonedDateTime.now().plusDays(1), amount);

        // Then
        assertThat(rewardUsdEquivalent.equivalenceSealingDate()).isEmpty();
    }

    @Test
    void should_return_reward_created_with_unlocked_token() {
        // When
        final var rewardUsdEquivalent = new RewardUsdEquivalent(rewardId, rewardCreatedAt.plusDays(1), currencyId, kycbVerifiedAt, currencyQuoteAvailableAt,
                unlockDate, amount);

        // Then
        assertThat(rewardUsdEquivalent.equivalenceSealingDate()).contains(rewardCreatedAt.plusDays(1));
    }

    @Test
    void should_return_kycb_verified_at_with_unlocked_token() {
        // When
        final var rewardUsdEquivalent = new RewardUsdEquivalent(rewardId, rewardCreatedAt, currencyId, kycbVerifiedAt.plusDays(1), currencyQuoteAvailableAt,
                unlockDate, amount);

        // Then
        assertThat(rewardUsdEquivalent.equivalenceSealingDate()).contains(kycbVerifiedAt.plusDays(1));
    }

    @Test
    void should_return_currency_quote_available_at_with_unlocked_token() {
        // When
        final var rewardUsdEquivalent = new RewardUsdEquivalent(rewardId, rewardCreatedAt, currencyId, kycbVerifiedAt, currencyQuoteAvailableAt.plusDays(1),
                unlockDate, amount);

        // Then
        assertThat(rewardUsdEquivalent.equivalenceSealingDate()).contains(currencyQuoteAvailableAt.plusDays(1));
    }

    @Test
    void should_return_unlock_date_for_locked_token() {
        // When
        final var rewardUsdEquivalent = new RewardUsdEquivalent(rewardId, rewardCreatedAt, currencyId, kycbVerifiedAt, currencyQuoteAvailableAt,
                unlockDate.plusDays(1), amount);

        // Then
        assertThat(rewardUsdEquivalent.equivalenceSealingDate()).contains(unlockDate.plusDays(1));
    }


    @Test
    void should_return_reward_created_at_with_not_locked_token() {
        // When
        final var rewardUsdEquivalent = new RewardUsdEquivalent(rewardId, rewardCreatedAt.plusDays(1), currencyId, kycbVerifiedAt, currencyQuoteAvailableAt,
                null, amount);

        // Then
        assertThat(rewardUsdEquivalent.equivalenceSealingDate()).contains(rewardCreatedAt.plusDays(1));
    }

    @Test
    void should_return_kycb_verified_at_with_not_locked_token() {
        // When
        final var rewardUsdEquivalent = new RewardUsdEquivalent(rewardId, rewardCreatedAt, currencyId, kycbVerifiedAt.plusDays(1), currencyQuoteAvailableAt,
                null, amount);

        // Then
        assertThat(rewardUsdEquivalent.equivalenceSealingDate()).contains(kycbVerifiedAt.plusDays(1));
    }

    @Test
    void should_return_currency_quote_available_at_with_not_locked_token() {
        // When
        final var rewardUsdEquivalent = new RewardUsdEquivalent(rewardId, rewardCreatedAt, currencyId, kycbVerifiedAt, currencyQuoteAvailableAt.plusDays(1),
                null, amount);

        // Then
        assertThat(rewardUsdEquivalent.equivalenceSealingDate()).contains(currencyQuoteAvailableAt.plusDays(1));
    }
}
