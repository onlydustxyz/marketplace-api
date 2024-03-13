package onlydust.com.marketplace.accounting.domain.model;

import lombok.NonNull;

import java.math.BigDecimal;

public record ConvertedAmount(@NonNull Amount convertedAmount, @NonNull BigDecimal conversionRate) {
}
