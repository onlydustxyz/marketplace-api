package onlydust.com.marketplace.api.bootstrap.it;

import com.fasterxml.jackson.core.JsonProcessingException;
import onlydust.com.marketplace.api.bootstrap.helper.HasuraJwtHelper;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.hasura.HasuraJwtPayload;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.jwt.JwtSecret;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.UUID;


@ActiveProfiles({"hasura_auth"})
public class HasuraAuthMeApiIT extends AbstractMarketplaceApiIT {
    final Long githubUserId = faker.number().randomNumber();
    final String login = faker.name().username();
    final String avatarUrl = faker.internet().avatar();
    final UUID userId = UUID.randomUUID();

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

    @Autowired
    JwtSecret jwtSecret;

    @Test
    void should_get_current_user_given_a_valid_jwt() throws JsonProcessingException {
        // Given
        final String jwt = HasuraJwtHelper.generateValidJwtFor(jwtSecret, HasuraJwtPayload.builder()
                .iss(jwtSecret.getIssuer())
                .claims(HasuraJwtPayload.HasuraClaims.builder()
                        .userId(userId)
                        .allowedRoles(List.of("me"))
                        .githubUserId(githubUserId)
                        .avatarUrl(avatarUrl)
                        .login(login)
                        .build())
                .build());

        // When
        client.get()
                .uri(getApiURI(ME_GET))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
                .exchange()
                // Then
                .expectStatus().is2xxSuccessful()
                .expectBody()
                .jsonPath("$.login").isEqualTo(login)
                .jsonPath("$.githubUserId").isEqualTo(githubUserId)
                .jsonPath("$.avatarUrl").isEqualTo(avatarUrl)
                .jsonPath("$.id").isEqualTo(userId.toString());
    }
}
