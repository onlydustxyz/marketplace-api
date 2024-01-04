package onlydust.com.marketplace.api.domain.view;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Data;
import onlydust.com.marketplace.api.domain.model.Currency;

@Data
@Builder
public class UserTotalRewardView {

  BigDecimal totalAmount;
  BigDecimal totalDollarsEquivalent;
  Currency currency;
}
