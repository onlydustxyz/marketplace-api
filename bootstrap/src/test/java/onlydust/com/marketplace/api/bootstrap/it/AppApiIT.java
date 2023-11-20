package onlydust.com.marketplace.api.bootstrap.it;

import onlydust.com.marketplace.api.bootstrap.MarketplaceApiApplicationIT;
import onlydust.com.marketplace.api.bootstrap.it.extension.PostgresITExtension;
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

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@ActiveProfiles({"hasura_auth", "it"})
@SpringBootTest(webEnvironment = RANDOM_PORT, classes = MarketplaceApiApplicationIT.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DirtiesContext
@ExtendWith(PostgresITExtension.class)
public class AppApiIT extends AbstractMarketplaceApiIT {
    @LocalServerPort
    int port;
    @Autowired
    WebTestClient client;

    @Test
    void should_redirect_root_path_to_actuator_health_check() {
        // When
        client.get()
                .uri("/")
                // Then
                .exchange()
                .expectStatus()
                .is3xxRedirection()
                .expectBody()
                .jsonPath("$.status", "UP");
    }


}
