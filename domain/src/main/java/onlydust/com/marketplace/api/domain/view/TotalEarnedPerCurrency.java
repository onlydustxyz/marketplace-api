package onlydust.com.marketplace.api.domain.view;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Data;
import onlydust.com.marketplace.api.domain.model.Currency;

@Data
@Builder
public class TotalEarnedPerCurrency {

  BigDecimal totalDollarsEquivalent;
  BigDecimal totalAmount;
  Currency currency;
}
