package onlydust.com.marketplace.api.read.entities;

import onlydust.com.marketplace.api.read.entities.user.PublicUserProfileResponseV2Entity;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class PublicUserProfileResponseV2EntityTest {

    @ParameterizedTest
    @CsvSource({
            "0, 0.1",
            "0.005, 1.0",
            "0.01, 1.0",
            "0.011, 5.0",
            "0.06, 10.0",
            "0.11, 100",
            "0.21, 100",
    })
    void prettyRankPercentile(BigDecimal rankPercentile, BigDecimal expectedPrettyRankPercentile) {
        final var prettyPercentile = PublicUserProfileResponseV2Entity.prettyRankPercentile(rankPercentile);
        assertThat(prettyPercentile).isEqualTo(expectedPrettyRankPercentile);
    }
}