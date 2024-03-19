package onlydust.com.marketplace.project.domain.view;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;

@Value
@Builder
@AllArgsConstructor
public class Money {
    BigDecimal amount;
    CurrencyView currency;
    BigDecimal usdEquivalent;
}
