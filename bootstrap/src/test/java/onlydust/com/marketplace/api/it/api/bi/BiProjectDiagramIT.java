package onlydust.com.marketplace.api.it.api.bi;

import onlydust.com.marketplace.api.it.api.AbstractMarketplaceApiIT;
import onlydust.com.marketplace.api.suites.tags.TagBI;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticationFilter.BEARER_PREFIX;

@TagBI
public class BiProjectDiagramIT extends AbstractMarketplaceApiIT {

    @Test
    public void should_get_aggregate_project_stats_for_diagram() {
        // When
        client.get()
                .uri(getApiURI(BI_STATS_PROJECTS, Map.of("timeGrouping", "WEEK")))
                .header("Authorization", BEARER_PREFIX + userAuthHelper.authenticateOlivier().jwt())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.stats").isArray();
    }

    @Test
    public void should_get_aggregate_project_stats_for_diagram_between_dates() {
        // When
        client.get()
                .uri(getApiURI(BI_STATS_PROJECTS, Map.of("timeGrouping", "DAY", "fromDate", "2022-01-01", "toDate", "2022-12-31")))
                .header("Authorization", BEARER_PREFIX + userAuthHelper.authenticateOlivier().jwt())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.stats").isArray()
                .jsonPath("$.stats.length()").isEqualTo(365)
                .jsonPath("$.stats[0].timestamp").isEqualTo("2022-01-01T00:00:00Z")
                .jsonPath("$.stats[364].timestamp").isEqualTo("2022-12-31T00:00:00Z");
    }
}
