package onlydust.com.marketplace.api.it.api;

import onlydust.com.marketplace.project.domain.port.input.LanguageFacadePort;
import onlydust.com.marketplace.project.domain.port.input.ProjectFacadePort;
import onlydust.com.marketplace.project.domain.port.input.UserFacadePort;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;


public class CronIT extends AbstractMarketplaceApiIT {

    @Autowired
    UserFacadePort userFacadePort;
    @Autowired
    LanguageFacadePort languageFacadePort;
    @Autowired
    ProjectFacadePort projectFacadePort;

    @Test
    void should_be_able_to_refresh_materialized_views() {
        assertDoesNotThrow(() -> userFacadePort.refreshUserRanksAndStats());
        assertDoesNotThrow(() -> languageFacadePort.updateProjectsLanguages());
        assertDoesNotThrow(() -> projectFacadePort.refreshRecommendations());
    }

    @Test
    void should_be_able_to_historize_ranks() {
        assertDoesNotThrow(() -> userFacadePort.historizeUserRanks());
    }
}
