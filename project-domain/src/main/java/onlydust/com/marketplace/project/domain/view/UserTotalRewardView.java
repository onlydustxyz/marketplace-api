package onlydust.com.marketplace.project.domain.view;

import lombok.Builder;
import lombok.Data;
import onlydust.com.marketplace.kernel.model.CurrencyView;

import java.math.BigDecimal;

@Data
@Builder
public class UserTotalRewardView {
    BigDecimal totalAmount;
    BigDecimal totalDollarsEquivalent;
    CurrencyView currency;
}
