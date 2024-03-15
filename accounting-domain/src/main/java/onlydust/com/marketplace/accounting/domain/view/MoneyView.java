package onlydust.com.marketplace.accounting.domain.view;

import lombok.Builder;
import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.model.Currency;

import java.math.BigDecimal;

@Builder(toBuilder = true)
public record MoneyView(@NonNull BigDecimal amount,
                        @NonNull Currency currency,
                        BigDecimal usdConversionRate,
                        BigDecimal dollarsEquivalent) {
}
