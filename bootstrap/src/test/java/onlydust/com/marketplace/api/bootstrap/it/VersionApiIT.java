package onlydust.com.marketplace.api.bootstrap.it;

import onlydust.com.marketplace.api.rest.api.adapter.mapper.DateMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;

import java.time.format.DateTimeFormatter;
import java.util.Date;

@ActiveProfiles({"hasura_auth"})
public class VersionApiIT extends AbstractMarketplaceApiIT {

    @Autowired
    Date startingDate;

    @Test
    void should_return_server_starting_date() {
        // When
        client.get()
                .uri("/api/v1/version")
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.releaseDate").isEqualTo(DateMapper.toZoneDateTime(startingDate)
                        .format(DateTimeFormatter.ISO_INSTANT));
    }
}
