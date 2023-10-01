package onlydust.com.marketplace.api.bootstrap.it;

import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

public class MeApiIT extends AbstractMarketplaceApiIT {

    @Order(1)
    @Test
    public void should_create_user_at_first_connection_given_a_valid_jwt() {
        client.get()
                .uri(getApiURI(ME_GET))
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful();
    }

}
