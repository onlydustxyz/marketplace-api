package onlydust.com.marketplace.kernel.mapper;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static java.lang.Math.*;

public interface AmountMapper {
    static BigDecimal pretty(BigDecimal amount, int decimals, BigDecimal usdRate) {
        final var optimumScale = usdRate == null ? decimals : max(0, (int) round(-log10(0.01 / usdRate.longValue())));

        return amount.setScale(min(amount.scale(), optimumScale), RoundingMode.HALF_EVEN);
    }
}
