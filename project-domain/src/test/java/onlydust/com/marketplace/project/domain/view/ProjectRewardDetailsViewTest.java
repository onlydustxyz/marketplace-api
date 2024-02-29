package onlydust.com.marketplace.project.domain.view;

import onlydust.com.marketplace.project.domain.model.Currency;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Java6Assertions.assertThat;

class ProjectRewardDetailsViewTest {
    @ParameterizedTest
    @EnumSource(value = Currency.class, names = {"APT", "USD", "ETH", "USDC", "LORDS"})
    void should_return_no_unlock_date_for_unlocked_tokens(Currency currency) {
        // Given
        final var view = ProjectRewardView.builder().amount(ProjectRewardView.Amount.builder().currency(currency).build()).build();

        // Then
        assertThat(view.getUnlockDate()).isNull();
    }

    @ParameterizedTest
    @EnumSource(value = Currency.class, names = {"STRK"})
    void should_return_no_unlock_date_for_indefinitely_locked_tokens(Currency currency) {
        // Given
        final var view = ProjectRewardView.builder().amount(ProjectRewardView.Amount.builder().currency(currency).build()).build();

        // Then
        assertThat(view.getUnlockDate()).isNull();
    }

    @ParameterizedTest
    @EnumSource(value = Currency.class, names = {"OP"})
    void should_return_unlock_date_for_locked_tokens(Currency currency) {
        // Given
        final var view = ProjectRewardView.builder().amount(ProjectRewardView.Amount.builder().currency(currency).build()).build();

        // Then
        assertThat(view.getUnlockDate()).isEqualTo("2024-08-23T00:00:00.000");
    }
}