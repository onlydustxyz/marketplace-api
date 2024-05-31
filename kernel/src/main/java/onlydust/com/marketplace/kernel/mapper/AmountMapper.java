package onlydust.com.marketplace.kernel.mapper;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static java.lang.Math.min;
import static onlydust.com.marketplace.kernel.Utils.CurrencyConversion.optimimumScale;

public interface AmountMapper {
    static BigDecimal pretty(BigDecimal amount, int decimals, BigDecimal usdRate) {
        return amount.setScale(min(amount.scale(), optimimumScale(usdRate, decimals)), RoundingMode.HALF_EVEN);
    }

    static BigDecimal prettyUsd(BigDecimal usdAmount) {
        if (usdAmount == null) return null;
        return pretty(usdAmount, 2, null);
    }
}
