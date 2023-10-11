package onlydust.com.marketplace.api.bootstrap.it;

import onlydust.com.marketplace.api.bootstrap.helper.ITAuthenticationContext;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.UserClaims;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.auth0.Auth0Authentication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class UsersApiIT extends AbstractMarketplaceApiIT {

    @Autowired
    ITAuthenticationContext authenticationContext;

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


        ((ITAuthenticationContext) authenticationContext).setAuthentication(Auth0Authentication.builder()
                .authorities(Stream.of("me").map(SimpleGrantedAuthority::new).collect(Collectors.toList()))
                .credentials(null)
                .isAuthenticated(true)
                .claims(UserClaims.builder()
                        .userId(anthonyId)
                        .login("AnthonyBuisset")
                        .githubUserId(31901905L)
                        .build())
                .principal("github|31901905")
                .build());

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
