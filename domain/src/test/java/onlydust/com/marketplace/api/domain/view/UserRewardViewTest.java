package onlydust.com.marketplace.api.domain.view;

import onlydust.com.marketplace.api.domain.model.Currency;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Java6Assertions.assertThat;

class UserRewardViewTest {
    @ParameterizedTest
    @EnumSource(value = Currency.class, names = {"Apt", "Usd", "Eth", "Usdc", "Lords"})
    void should_return_no_unlock_date_for_unlocked_tokens(Currency currency) {
        // Given
        final var view = UserRewardView.builder().amount(UserRewardView.RewardAmountView.builder().currency(currency).build()).build();

        // Then
        assertThat(view.getUnlockDate()).isNull();
    }

    @ParameterizedTest
    @EnumSource(value = Currency.class, names = {"Strk"})
    void should_return_no_unlock_date_for_indefinitely_locked_tokens(Currency currency) {
        // Given
        final var view = UserRewardView.builder().amount(UserRewardView.RewardAmountView.builder().currency(currency).build()).build();

        // Then
        assertThat(view.getUnlockDate()).isNull();
    }

    @ParameterizedTest
    @EnumSource(value = Currency.class, names = {"Op"})
    void should_return_unlock_date_for_locked_tokens(Currency currency) {
        // Given
        final var view = UserRewardView.builder().amount(UserRewardView.RewardAmountView.builder().currency(currency).build()).build();

        // Then
        assertThat(view.getUnlockDate()).isEqualTo("2024-08-23T00:00:00.000");
    }
}