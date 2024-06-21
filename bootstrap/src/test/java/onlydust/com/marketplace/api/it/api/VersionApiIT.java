package onlydust.com.marketplace.api.it.api;

import onlydust.com.marketplace.api.contract.model.GetAPIVersion200Response;
import onlydust.com.marketplace.api.rest.api.adapter.mapper.DateMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;


public class VersionApiIT extends AbstractMarketplaceApiIT {

    @Autowired
    Date startingDate;

    @Test
    void should_return_server_starting_date() {
        // Given
        final var response = new GetAPIVersion200Response()
                .releaseDate(DateMapper.toZoneDateTime(startingDate));

        // When
        client.get()
                .uri("/api/v1/version")
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .equals(response);
    }
}
