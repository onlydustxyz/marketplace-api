package onlydust.com.marketplace.api.domain.view;

import onlydust.com.marketplace.api.domain.model.Currency;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Java6Assertions.assertThat;

class ProjectRewardViewTest {
    @ParameterizedTest
    @EnumSource(value = Currency.class, names = {"Apt", "Usd", "Eth", "Usdc", "Lords"})
    void should_return_no_unlock_date_for_unlocked_tokens(Currency currency) {
        // Given
        final var view = ProjectRewardView.builder().amount(ProjectRewardView.RewardAmountView.builder().currency(currency).build()).build();

        // Then
        assertThat(view.getUnlockDate()).isNull();
    }

    @ParameterizedTest
    @EnumSource(value = Currency.class, names = {"Strk", "Op"})
    void should_return_no_unlock_date_for_indefinitely_locked_tokens(Currency currency) {
        // Given
        final var view = ProjectRewardView.builder().amount(ProjectRewardView.RewardAmountView.builder().currency(currency).build()).build();

        // Then
        assertThat(view.getUnlockDate()).isNull();
    }
}