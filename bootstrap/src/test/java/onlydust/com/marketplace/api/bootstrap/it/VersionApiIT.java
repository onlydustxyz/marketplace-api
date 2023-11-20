package onlydust.com.marketplace.api.bootstrap.it;

import onlydust.com.marketplace.api.bootstrap.MarketplaceApiApplicationIT;
import onlydust.com.marketplace.api.bootstrap.it.extension.PostgresITExtension;
import onlydust.com.marketplace.api.contract.model.InlineResponse200;
import onlydust.com.marketplace.api.rest.api.adapter.mapper.DateMapper;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.Date;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@ActiveProfiles({"hasura_auth", "it"})
@SpringBootTest(webEnvironment = RANDOM_PORT, classes = MarketplaceApiApplicationIT.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DirtiesContext
@ExtendWith(PostgresITExtension.class)
public class VersionApiIT extends AbstractMarketplaceApiIT {
    @LocalServerPort
    int port;
    @Autowired
    WebTestClient client;
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
