package onlydust.com.marketplace.api.domain.view;

import lombok.Builder;
import lombok.Data;
import onlydust.com.marketplace.api.domain.model.Currency;

import java.math.BigDecimal;

@Data
@Builder
public class TotalEarnedPerCurrency {
    BigDecimal totalDollarsEquivalent;
    BigDecimal totalAmount;
    Currency currency;
}
