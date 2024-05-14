package onlydust.com.marketplace.bff.read.entities;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class PublicUserProfileResponseV2EntityTest {

    @ParameterizedTest
    @CsvSource({
            "0, 1",
            "0.005, 1",
            "0.01, 1",
            "0.011, 5",
            "0.06, 10",
            "0.11, 20",
            "0.21, 100",
    })
    void prettyRankPercentile(BigDecimal rankPercentile, Integer expectedPrettyRankPercentile) {
        final var prettyPercentile = PublicUserProfileResponseV2Entity.prettyRankPercentile(rankPercentile);
        assertThat(prettyPercentile).isEqualTo(expectedPrettyRankPercentile);
    }
}