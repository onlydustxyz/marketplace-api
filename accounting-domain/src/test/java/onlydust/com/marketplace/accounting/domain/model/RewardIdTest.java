package onlydust.com.marketplace.accounting.domain.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class RewardIdTest {

    @Test
    void should_compute_pretty_id() {
        // Given
        final RewardId rewardId = RewardId.random();

        // When
        final String prettyId = rewardId.pretty();

        // Then
        Assertions.assertEquals("#" + rewardId.toString().substring(0, 5).toUpperCase(), prettyId);
    }
}
