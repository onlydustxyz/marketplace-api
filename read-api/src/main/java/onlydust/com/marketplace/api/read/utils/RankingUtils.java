package onlydust.com.marketplace.api.read.utils;

import java.math.BigDecimal;
import java.util.stream.Stream;

public class RankingUtils {
    public static BigDecimal prettyRankPercentile(BigDecimal rankPercentile) {
        final var percent = rankPercentile.multiply(BigDecimal.valueOf(100)).doubleValue();
        return Stream.of(0.1D, 1D, 5D, 10D)
                .filter(i -> percent <= i)
                .map(BigDecimal::valueOf)
                .min(BigDecimal::compareTo)
                .orElse(BigDecimal.valueOf(100));
    }
} 