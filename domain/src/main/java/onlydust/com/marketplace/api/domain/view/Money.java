package onlydust.com.marketplace.api.domain.view;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import onlydust.com.marketplace.api.domain.model.Currency;

import java.math.BigDecimal;

@Value
@Builder
@AllArgsConstructor
public class Money {
    BigDecimal amount;
    Currency currency;
    BigDecimal usdEquivalent;
}
