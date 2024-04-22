package onlydust.com.marketplace.project.domain.view;

import lombok.Value;

import java.math.BigDecimal;
import java.util.List;

@Value
public class TotalsEarned {
    BigDecimal totalDollarsEquivalent;
    List<Money> details;

    public TotalsEarned(List<Money> totalEarnedPerCurrencies) {
        this.details = totalEarnedPerCurrencies;
        totalDollarsEquivalent = totalEarnedPerCurrencies.stream()
                .map(money -> money.dollarsEquivalent().orElse(BigDecimal.ZERO))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
