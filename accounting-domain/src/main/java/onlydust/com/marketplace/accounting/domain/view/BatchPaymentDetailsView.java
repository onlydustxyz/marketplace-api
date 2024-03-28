package onlydust.com.marketplace.accounting.domain.view;

import lombok.Builder;
import lombok.NonNull;
import lombok.experimental.Accessors;
import onlydust.com.marketplace.accounting.domain.model.Payment;

import java.math.BigDecimal;
import java.util.List;

import static java.util.stream.Collectors.groupingBy;
import static onlydust.com.marketplace.kernel.exception.OnlyDustException.internalServerError;

@Builder
@Accessors(chain = true, fluent = true)
public record BatchPaymentDetailsView(
        @NonNull Payment payment,
        @NonNull
        List<RewardDetailsView> rewardViews
) {

    public List<TotalMoneyView> totalsPerCurrency() {
        return rewardViews.stream()
                .collect(groupingBy(r -> r.money().currency()))
                .entrySet()
                .stream()
                .map(e -> new TotalMoneyView(
                        e.getValue().stream().map(r -> r.money().amount()).reduce(BigDecimal::add).orElseThrow(),
                        e.getKey(),
                        e.getValue().stream()
                                .map(r -> r.money().dollarsEquivalent().orElseThrow(() -> internalServerError("Dollars equivalent not found for reward %s".formatted(r.id().value()))))
                                .reduce(BigDecimal::add).orElseThrow()
                )).toList();
    }
}
