package onlydust.com.marketplace.api.bootstrap.it.api;

import onlydust.com.marketplace.api.contract.model.InlineResponse200;
import onlydust.com.marketplace.api.rest.api.adapter.mapper.DateMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;

import java.util.Date;

@ActiveProfiles({"hasura_auth"})
public class VersionApiIT extends AbstractMarketplaceApiIT {

    @Autowired
    Date startingDate;

    @Test
    void should_return_server_starting_date() {
        // Given
        final InlineResponse200 inlineResponse200 = new InlineResponse200()
                .releaseDate(DateMapper.toZoneDateTime(startingDate));

        // When
        client.get()
                .uri("/api/v1/version")
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .equals(inlineResponse200);
    }
}
