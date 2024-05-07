package onlydust.com.marketplace.api.bootstrap.it.api;

import org.junit.jupiter.api.Test;


public class UsersReadApiIT extends AbstractMarketplaceApiIT {

    @Test
    void should_return_users_languages_stats() {
        // Given
        final var user = userAuthHelper.authenticateAnthony().user();

        // When
        client.get()
                .uri(getApiURI(USER_LANGUAGES.formatted(user.getGithubUserId())))
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .consumeWith(System.out::println);
    }
}
