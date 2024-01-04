package onlydust.com.marketplace.api.domain.view;

import static java.util.Objects.isNull;

import java.math.BigDecimal;
import java.util.List;
import onlydust.com.marketplace.api.domain.model.Currency;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class UserRewardTotalAmountsViewTest {

  @Test
  void should_compute_dollars_total_amount() {
    // Given
    final UserRewardTotalAmountsView userRewardTotalAmountsView =
        UserRewardTotalAmountsView.builder().userTotalRewards(
            List.of(
                getStub(10, 1000D, Currency.Eth),
                getStub(20, 2000D, Currency.Eth),
                getStub(500, 600D, Currency.Apt),
                getStub(5000, 5000D, Currency.Usd),
                getStub(1000, null, Currency.Strk)
            )
        ).build();

    // Then
    Assertions.assertEquals(BigDecimal.valueOf(8600D), userRewardTotalAmountsView.getTotalAmount());
  }

  private static UserTotalRewardView getStub(final double amount, final Double dollarsEquivalent,
      final Currency currency) {
    return UserTotalRewardView.builder()
        .totalAmount(BigDecimal.valueOf(amount))
        .totalDollarsEquivalent(isNull(dollarsEquivalent) ? null : BigDecimal.valueOf(dollarsEquivalent))
        .currency(currency)
        .build();
  }
}
