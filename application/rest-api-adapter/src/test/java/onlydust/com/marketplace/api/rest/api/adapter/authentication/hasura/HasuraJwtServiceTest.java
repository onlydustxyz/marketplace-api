package onlydust.com.marketplace.api.rest.api.adapter.authentication.hasura;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.javafaker.Faker;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.jwt.JwtSecret;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class HasuraJwtServiceTest {

    private final static Faker faker = new Faker();

    @Test
    void should_authenticate_given_a_valid_jwt() throws JsonProcessingException {
        // Given
        final JwtSecret jwtSecret = JwtSecret.builder().key(faker.cat().name()).issuer(faker.cat().breed()).type(
                "HS256").build();
        final HasuraJwtService hasuraJwtService =
                new HasuraJwtService(JwtHelper.buildHasuraPropertiesFromSecret(jwtSecret));
        final String jwtToken = JwtHelper.generateValidJwtFor(jwtSecret, HasuraJwtPayload.builder()
                .iss(jwtSecret.getIssuer())
                .build());

        // When
        final Authentication authenticationFromJWT = hasuraJwtService.getAuthenticationFromJwt(jwtToken);

        // Then
        assertTrue(authenticationFromJWT.isAuthenticated());
    }

}
