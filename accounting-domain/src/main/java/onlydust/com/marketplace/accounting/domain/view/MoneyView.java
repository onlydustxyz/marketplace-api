package onlydust.com.marketplace.accounting.domain.view;

import lombok.Builder;
import lombok.NonNull;

import java.math.BigDecimal;

@Builder
public record MoneyView(@NonNull BigDecimal amount,
                        @NonNull String currencyName,
                        @NonNull String currencyCode,
                        @NonNull String currencyLogoUrl,
                        @NonNull BigDecimal dollarsEquivalent) {
}
