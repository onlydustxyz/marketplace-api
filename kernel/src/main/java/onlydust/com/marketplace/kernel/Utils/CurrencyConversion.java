package onlydust.com.marketplace.kernel.Utils;

import java.math.BigDecimal;

import static java.lang.Math.*;

public interface CurrencyConversion {
    static int optimimumScale(BigDecimal usdRate, int decimals) {
        return usdRate == null ? decimals :
                min(decimals, max(0, (int) round(-log10(0.01 / usdRate.longValue()))));
    }
}
