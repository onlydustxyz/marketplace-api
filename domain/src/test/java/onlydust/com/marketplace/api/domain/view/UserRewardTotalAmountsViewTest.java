package onlydust.com.marketplace.api.domain.view;

import onlydust.com.marketplace.api.domain.model.Currency;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static java.util.Objects.isNull;

public class UserRewardTotalAmountsViewTest {

    @Test
    void should_compute_dollars_total_amount() {
        // Given
        final UserRewardTotalAmountsView userRewardTotalAmountsView =
                UserRewardTotalAmountsView.builder().userTotalRewards(
                        List.of(
                                getStub(10, 1000D, Currency.ETH),
                                getStub(20, 2000D, Currency.ETH),
                                getStub(500, 600D, Currency.APT),
                                getStub(5000, 5000D, Currency.USD),
                                getStub(1000, null, Currency.STRK)
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
