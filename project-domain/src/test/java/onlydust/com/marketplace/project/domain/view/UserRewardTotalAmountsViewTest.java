package onlydust.com.marketplace.project.domain.view;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static java.util.Objects.isNull;

public class UserRewardTotalAmountsViewTest {

    final static CurrencyView ETH = CurrencyView.builder().id(CurrencyView.Id.random()).code("ETH").name("Ethereum").decimals(18).build();
    final static CurrencyView USD = CurrencyView.builder().id(CurrencyView.Id.random()).code("USD").name("Dollar").decimals(2).build();
    final static CurrencyView STRK = CurrencyView.builder().id(CurrencyView.Id.random()).code("STRK").name("Starknet Coin").decimals(18).build();
    final static CurrencyView LORDS = CurrencyView.builder().id(CurrencyView.Id.random()).code("LORDS").name("Lords").decimals(18).build();

    @Test
    void should_compute_dollars_total_amount() {
        // Given
        final UserRewardTotalAmountsView userRewardTotalAmountsView =
                UserRewardTotalAmountsView.builder().userTotalRewards(
                        List.of(
                                getStub(10, 1000D, ETH),
                                getStub(20, 2000D, ETH),
                                getStub(500, 600D, LORDS),
                                getStub(5000, 5000D, ETH),
                                getStub(1000, null, STRK)
                        )
                ).build();

        // Then
        Assertions.assertEquals(BigDecimal.valueOf(8600D), userRewardTotalAmountsView.getTotalAmount());
    }

    private static UserTotalRewardView getStub(final double amount, final Double dollarsEquivalent,
                                               final CurrencyView currency) {
        return UserTotalRewardView.builder()
                .totalAmount(BigDecimal.valueOf(amount))
                .totalDollarsEquivalent(isNull(dollarsEquivalent) ? null : BigDecimal.valueOf(dollarsEquivalent))
                .currency(currency)
                .build();
    }
}
