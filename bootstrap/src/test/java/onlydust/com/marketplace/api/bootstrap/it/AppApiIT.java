package onlydust.com.marketplace.api.bootstrap.it;

import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles({"hasura_auth"})
public class AppApiIT extends AbstractMarketplaceApiIT {


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
