package onlydust.com.marketplace.project.domain.view;

import lombok.Value;

import java.math.BigDecimal;
import java.util.List;

@Value
public class TotalsEarned {
    BigDecimal totalDollarsEquivalent;
    List<TotalEarnedPerCurrency> details;

    public TotalsEarned(List<TotalEarnedPerCurrency> totalEarnedPerCurrencies) {
        this.details = totalEarnedPerCurrencies;
        totalDollarsEquivalent = totalEarnedPerCurrencies.stream()
                .map(TotalEarnedPerCurrency::getTotalDollarsEquivalent)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
