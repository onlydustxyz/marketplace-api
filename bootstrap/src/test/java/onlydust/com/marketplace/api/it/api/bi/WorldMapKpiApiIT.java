package onlydust.com.marketplace.api.it.api.bi;

import onlydust.com.marketplace.api.it.api.AbstractMarketplaceApiIT;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;

public class WorldMapKpiApiIT extends AbstractMarketplaceApiIT {
    @Nested
    class ActiveContributors {
        @Test
        void should_get_active_contributors_by_country() {
            client.get()
                    .uri(getApiURI(BI_WORLD_MAP, Map.of(
                            "kpi", "ACTIVE_CONTRIBUTORS"
                    )))
                    .exchange()
                    .expectStatus()
                    .isOk()
                    .expectBody()
                    .json("""
                            [
                              {
                                "countryCode": "FRA",
                                "value": 1
                              }
                            ]
                            """);
        }
    }
}
