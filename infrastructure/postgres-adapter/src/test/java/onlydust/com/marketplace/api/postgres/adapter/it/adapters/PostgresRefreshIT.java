package onlydust.com.marketplace.api.postgres.adapter.it.adapters;

import onlydust.com.marketplace.api.postgres.adapter.it.AbstractPostgresIT;
import onlydust.com.marketplace.project.domain.port.output.ProjectStoragePort;
import onlydust.com.marketplace.project.domain.port.output.UserStoragePort;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class PostgresRefreshIT extends AbstractPostgresIT {
    @Autowired
    private ProjectStoragePort projectStoragePort;
    @Autowired
    private UserStoragePort userStoragePort;

    @Test
    void should_be_able_to_refresh_materialized_views() {
        assertDoesNotThrow(() -> userStoragePort.refreshUserRanksAndStats());
        assertDoesNotThrow(() -> projectStoragePort.refreshRecommendations());
        assertDoesNotThrow(() -> projectStoragePort.refreshStats());
    }
}
