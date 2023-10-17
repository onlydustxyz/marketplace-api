package onlydust.com.marketplace.api.bootstrap.it;

import com.auth0.jwt.interfaces.JWTVerifier;
import onlydust.com.marketplace.api.bootstrap.helper.JwtVerifierStub;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;

public class MeApiIT extends AbstractMarketplaceApiIT {
    final static String JWT_TOKEN = "fake-jwt";
    final Long githubUserId = faker.number().randomNumber();
    final String login = faker.name().username();
    final String avatarUrl = faker.internet().avatar();

    @Autowired
    JWTVerifier jwtVerifier;

    @BeforeEach
    void setup() {
        ((JwtVerifierStub) jwtVerifier).withJwtMock(JWT_TOKEN, githubUserId, login, avatarUrl);
    }

    @Test
    public void should_be_unauthorized() {
        // When
        client.get()
                .uri(getApiURI(ME_GET))
                // Then
                .exchange()
                .expectStatus()
                .isEqualTo(401);
    }


    @Test
    void should_get_current_user_given_a_valid_jwt() {
        // Given

        // When
        client.get()
                .uri(getApiURI(ME_GET))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + JWT_TOKEN)
                .exchange()
                // Then
                .expectStatus().is2xxSuccessful()
                .expectBody()
                .jsonPath("$.login").isEqualTo(login)
                .jsonPath("$.githubUserId").isEqualTo(githubUserId)
                .jsonPath("$.avatarUrl").isEqualTo(avatarUrl)
                .jsonPath("$.id").isNotEmpty();
    }
}
