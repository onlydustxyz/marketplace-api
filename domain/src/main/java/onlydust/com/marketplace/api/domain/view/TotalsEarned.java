package onlydust.com.marketplace.api.domain.view;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
public class TotalsEarned {
    BigDecimal totalDollarsEquivalent;
    @Builder.Default
    List<TotalEarnedPerCurrency> details = new ArrayList<>();

    public void addDetail(final TotalEarnedPerCurrency totalEarnedPerCurrency){
        this.details.add(totalEarnedPerCurrency);
    }
}
