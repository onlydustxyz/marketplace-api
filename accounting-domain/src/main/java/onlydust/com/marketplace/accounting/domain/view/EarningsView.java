package onlydust.com.marketplace.accounting.domain.view;

import lombok.NonNull;

import java.math.BigDecimal;
import java.util.List;

public record EarningsView(@NonNull List<EarningsPerCurrency> earningsPerCurrencies) {

    public BigDecimal totalUsdAmount() {
        return earningsPerCurrencies.stream()
                .map(earnings -> earnings.money().dollarsEquivalent())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public record EarningsPerCurrency(@NonNull TotalMoneyView money,
                                      @NonNull Long rewardCount) {
    }
}
