package onlydust.com.marketplace.api.it.api;

import onlydust.com.marketplace.api.postgres.adapter.PostgresRecommenderSystemV1Adapter;
import onlydust.com.marketplace.project.domain.port.input.UserFacadePort;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;


public class CronIT extends AbstractMarketplaceApiIT {

    @Autowired
    UserFacadePort userFacadePort;
    @Autowired
    PostgresRecommenderSystemV1Adapter postgresRecommenderSystemV1Adapter;

    @Test
    void should_be_able_to_historize_ranks() {
        assertDoesNotThrow(() -> userFacadePort.historizeUserRanks());
        assertDoesNotThrow(() -> postgresRecommenderSystemV1Adapter.refreshData());
    }
}
