package onlydust.com.marketplace.api.rest.api.adapter.authentication.hasura;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.javafaker.Faker;
import onlydust.com.marketplace.api.domain.exception.OnlydustException;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.jwt.JwtSecret;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;

import static org.junit.jupiter.api.Assertions.*;

public class HasuraJwtServiceTest {

    private final static Faker faker = new Faker();

    @Test
    void should_authenticate_given_a_valid_jwt() throws JsonProcessingException {
        // Given
        final JwtSecret jwtSecret = JwtSecret.builder().key(faker.cat().name()).issuer(faker.cat().breed()).type(
                "HS256").build();
        final HasuraJwtService hasuraJwtService = new HasuraJwtService(jwtSecret);
        final HasuraJwtPayload hasuraJwtPayload =
                HasuraJwtPayload.builder().iss(jwtSecret.getIssuer()).sub(faker.rickAndMorty().character()).build();
        final String jwtToken = JwtHelper.generateValidJwtFor(jwtSecret, hasuraJwtPayload);

        // When
        final Authentication authenticationFromJwt = hasuraJwtService.getAuthenticationFromJwt(jwtToken);

        // Then
        assertTrue(authenticationFromJwt.isAuthenticated());
        assertEquals(authenticationFromJwt.getName(), hasuraJwtPayload.getSub());
        assertEquals(authenticationFromJwt.getPrincipal(), hasuraJwtPayload.getSub());
        assertEquals(authenticationFromJwt.getCredentials(), hasuraJwtPayload);
        assertEquals(authenticationFromJwt.getDetails(), hasuraJwtPayload.getClaims());
    }


    @Test
    void should_throw_invalid_jwt_format_exception() {
        // Given
        final JwtSecret jwtSecret = JwtSecret.builder().key(faker.cat().name()).issuer(faker.cat().breed()).type(
                "HS256").build();
        final HasuraJwtService hasuraJwtService = new HasuraJwtService(jwtSecret);

        // When
        final HasuraAuthentication hasuraAuthentication =
                hasuraJwtService.getAuthenticationFromJwt(faker.chuckNorris().fact());

        // Then
        final OnlydustException onlydustException = hasuraAuthentication.getOnlydustException();
        assertNotNull(onlydustException);
        assertEquals(401, onlydustException.getStatus());
    }


    @Test
    void should_throw_invalid_header_format_exception() {
        // Given
        final JwtSecret jwtSecret = JwtSecret.builder().key(faker.cat().name()).issuer(faker.cat().breed()).type(
                "HS256").build();
        final HasuraJwtService hasuraJwtService = new HasuraJwtService(jwtSecret);
        final String jwtToken =
                faker.cat().name() + "." + faker.pokemon().name() + "." + faker.pokemon().name();

        // When
        final HasuraAuthentication hasuraAuthentication = hasuraJwtService.getAuthenticationFromJwt(jwtToken);

        // Then
        final OnlydustException onlydustException = hasuraAuthentication.getOnlydustException();
        assertNotNull(onlydustException);
        assertEquals(401, onlydustException.getStatus());
    }

    @Test
    void should_throw_unable_to_deserialize_jwt() throws JsonProcessingException {
        // Given
        final JwtSecret jwtSecret = JwtSecret.builder().key(faker.cat().name()).issuer(faker.cat().breed()).type(
                "HS256").build();
        final HasuraJwtService hasuraJwtService = new HasuraJwtService(jwtSecret);
        final String jwtToken = JwtHelper.generateValidJwtFor(jwtSecret, faker.pokemon().name());

        // When
        final HasuraAuthentication hasuraAuthentication =
                hasuraJwtService.getAuthenticationFromJwt(jwtToken);

        // Then
        final OnlydustException onlydustException = hasuraAuthentication.getOnlydustException();
        assertNotNull(onlydustException);
        assertEquals(401, onlydustException.getStatus());
    }
}
