package onlydust.com.marketplace.accounting.domain.view;

import lombok.Builder;
import lombok.NonNull;

import java.math.BigDecimal;

@Builder(toBuilder = true)
public record MoneyView(@NonNull BigDecimal amount,
                        @NonNull String currencyName,
                        @NonNull String currencyCode,
                        String currencyLogoUrl,
                        @NonNull BigDecimal dollarsEquivalent) {
}
