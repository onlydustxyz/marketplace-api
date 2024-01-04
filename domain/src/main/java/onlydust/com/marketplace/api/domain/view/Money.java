package onlydust.com.marketplace.api.domain.view;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import onlydust.com.marketplace.api.domain.model.Currency;

@Value
@Builder
@AllArgsConstructor
public class Money {

  BigDecimal amount;
  Currency currency;
  BigDecimal usdEquivalent;
}
