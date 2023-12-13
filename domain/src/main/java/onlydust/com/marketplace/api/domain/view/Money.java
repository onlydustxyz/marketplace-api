package onlydust.com.marketplace.api.domain.view;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Value;
import onlydust.com.marketplace.api.domain.model.Currency;
import onlydust.com.marketplace.api.domain.view.pagination.Page;

import java.math.BigDecimal;

@Value
@Builder
@AllArgsConstructor
public class Money {
    BigDecimal amount;
    Currency currency;
    BigDecimal usdEquivalent;
}
