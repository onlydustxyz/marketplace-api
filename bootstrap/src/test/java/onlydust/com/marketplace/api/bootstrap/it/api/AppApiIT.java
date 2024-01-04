package onlydust.com.marketplace.api.bootstrap.it.api;

import org.junit.jupiter.api.Test;


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
