package onlydust.com.marketplace.api.bootstrap.it;

import org.junit.jupiter.api.Test;

import java.util.UUID;

public class UsersApiIT extends AbstractMarketplaceApiIT {

    @Test
    void should_return_a_not_found_error() {
        // Given
        final UUID notExistingUserId = UUID.randomUUID();

        // When
        client.get()
                .uri(getApiURI(USERS_GET + "/" + notExistingUserId))
                .exchange()
                // Then
                .expectStatus()
                .isEqualTo(404);
    }

    @Test
    void should_get_user_profile() {
        // Given
        final UUID anthonyId = UUID.fromString("747e663f-4e68-4b42-965b-b5aebedcd4c4");

        // When
        client.get()
                .uri(getApiURI(USERS_GET + "/" + anthonyId))
                .exchange()
                // Then
                .expectStatus().is2xxSuccessful()
                .expectBody()
                .jsonPath("$.login").isEqualTo("AnthonyBuisset")
                .jsonPath("$.id").isEqualTo(anthonyId.toString());
    }
}
