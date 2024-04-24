package onlydust.com.marketplace.kernel.mapper;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static java.lang.Math.*;

public interface AmountMapper {
    static BigDecimal pretty(BigDecimal amount, int decimals, BigDecimal usdRate) {
        final var optimumScale = usdRate == null ? decimals : max(0, (int) round(-log10(0.01 / usdRate.longValue())));

        return amount.setScale(min(decimals, min(amount.scale(), optimumScale)), RoundingMode.HALF_EVEN);
    }

    static BigDecimal prettyUsd(BigDecimal usdAmount) {
        if (usdAmount == null) return null;
        return pretty(usdAmount, 2, null);
    }
}
