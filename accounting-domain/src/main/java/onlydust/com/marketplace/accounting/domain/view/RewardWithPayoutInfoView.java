package onlydust.com.marketplace.accounting.domain.view;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import lombok.experimental.Accessors;
import onlydust.com.marketplace.accounting.domain.model.RewardId;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.PayoutInfo;

import java.math.BigDecimal;
import java.util.Optional;

@AllArgsConstructor
@Data
@Accessors(fluent = true)
public class RewardWithPayoutInfoView {
    @NonNull RewardId id;
    PayoutInfo payoutInfo;
    @NonNull BigDecimal usdConversionRate;

    public Optional<PayoutInfo> payoutInfo() {
        return Optional.ofNullable(payoutInfo);
    }
}
