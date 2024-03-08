package onlydust.com.marketplace.accounting.domain.view;

import lombok.Builder;
import lombok.NonNull;
import lombok.experimental.Accessors;
import onlydust.com.marketplace.accounting.domain.model.BatchPayment;

import java.util.List;

@Builder
@Accessors(chain = true, fluent = true)
public record BatchPaymentDetailsView(
        @NonNull
        BatchPayment batchPayment,
        @NonNull
        List<BackofficeRewardView> rewardViews
) {
}
