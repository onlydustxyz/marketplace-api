package onlydust.com.marketplace.api.bootstrap.it;

import com.fasterxml.jackson.core.JsonProcessingException;
import onlydust.com.marketplace.api.bootstrap.helper.JwtHelper;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.hasura.HasuraJwtPayload;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.jwt.JwtSecret;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;

import java.util.List;
import java.util.UUID;

public class MeApiIT extends AbstractMarketplaceApiIT {

    @Autowired
    JwtSecret jwtSecret;

    @Test
    public void should_be_unauthorized() {
        // When
        client.get()
                .uri(getApiURI(ME_GET))
                // Then
                .exchange()
                .expectStatus()
                .isEqualTo(403);
    }

    @Test
    void should_get_current_user_given_a_valid_hasura_jwt() throws JsonProcessingException {
        // Given
        final UUID userId = UUID.randomUUID();
        final long githubUserId = faker.number().randomNumber();
        final String hasuraJwt = JwtHelper.generateValidJwtFor(jwtSecret, HasuraJwtPayload.builder()
                .sub(userId.toString())
                .iss(jwtSecret.getIssuer())
                .claims(HasuraJwtPayload.HasuraClaims.builder()
                        .githubUserId(githubUserId)
                        .userId(userId)
                        .allowedRoles(List.of(faker.pokemon().name(), "me"))
                        .build())
                .build());

        // When
        client.get()
                .uri(getApiURI(ME_GET))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + hasuraJwt)
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.id").isEqualTo(userId.toString())
                .jsonPath("$.githubUserId").isEqualTo(githubUserId);
    }
}
