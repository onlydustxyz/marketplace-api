package onlydust.com.marketplace.project.domain.view;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class UserTotalRewardView {
    BigDecimal totalAmount;
    BigDecimal totalDollarsEquivalent;
    CurrencyView currency;
}
