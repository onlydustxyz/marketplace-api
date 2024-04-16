package onlydust.com.marketplace.accounting.domain.view;

import lombok.Builder;
import lombok.Value;
import lombok.experimental.Accessors;
import onlydust.com.marketplace.accounting.domain.model.Network;
import onlydust.com.marketplace.accounting.domain.model.Payment;

import java.time.ZonedDateTime;
import java.util.List;

@Builder
@Value
@Accessors(chain = true, fluent = true)
public class BatchPaymentShortView {
    Payment.Id id;
    ZonedDateTime createdAt;
    Payment.Status status;
    Network network;
    Long rewardCount;
    List<TotalMoneyView> totalsPerCurrency;
}
