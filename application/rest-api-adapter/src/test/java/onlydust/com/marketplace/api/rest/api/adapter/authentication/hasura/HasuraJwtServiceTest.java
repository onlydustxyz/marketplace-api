package onlydust.com.marketplace.api.rest.api.adapter.authentication.hasura;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.javafaker.Faker;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.OnlyDustAuthentication;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.jwt.JwtSecret;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class HasuraJwtServiceTest {

    private final static Faker faker = new Faker();

    @Test
    void should_authenticate_given_a_valid_jwt() throws JsonProcessingException {
        // Given
        final JwtSecret jwtSecret = JwtSecret.builder().key(faker.cat().name()).issuer(faker.cat().breed()).type(
                "HS256").build();
        final HasuraJwtService hasuraJwtService = new HasuraJwtService(jwtSecret);
        final HasuraJwtPayload hasuraJwtPayload =
                HasuraJwtPayload.builder()
                        .iss(jwtSecret.getIssuer())
                        .sub(faker.rickAndMorty().character())
                        .claims(
                                HasuraJwtPayload.HasuraClaims.builder()
                                        .userId(UUID.randomUUID())
                                        .build()
                        )
                        .build();
        final String jwtToken = JwtHelper.generateValidJwtFor(jwtSecret, hasuraJwtPayload);

        // When
        final Optional<OnlyDustAuthentication> authentication = hasuraJwtService.getAuthenticationFromJwt(jwtToken);

        // Then
        assertThat(authentication).isPresent();
        final var authenticationFromJwt = authentication.get();
        assertTrue(authenticationFromJwt.isAuthenticated());
        assertEquals(authenticationFromJwt.getName(), hasuraJwtPayload.getSub());
        assertEquals(authenticationFromJwt.getPrincipal(), hasuraJwtPayload.getSub());
        assertEquals(authenticationFromJwt.getCredentials(), hasuraJwtPayload);
        assertEquals(authenticationFromJwt.getUser().getId(), hasuraJwtPayload.getClaims().getUserId());
        assertEquals(authenticationFromJwt.getUser().getPermissions(), hasuraJwtPayload.getClaims().getAllowedRoles());
        assertEquals(authenticationFromJwt.getUser().getGithubUserId(), hasuraJwtPayload.getClaims().getGithubUserId());
    }


    @Test
    void should_throw_invalid_jwt_format_exception() {
        // Given
        final JwtSecret jwtSecret = JwtSecret.builder().key(faker.cat().name()).issuer(faker.cat().breed()).type(
                "HS256").build();
        final HasuraJwtService hasuraJwtService = new HasuraJwtService(jwtSecret);

        // When
        final Optional<OnlyDustAuthentication> authentication =
                hasuraJwtService.getAuthenticationFromJwt(faker.chuckNorris().fact());

        // Then
        assertThat(authentication).isNotPresent();
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
        final Optional<OnlyDustAuthentication> authentication = hasuraJwtService.getAuthenticationFromJwt(jwtToken);

        // Then
        assertThat(authentication).isNotPresent();
    }

    @Test
    void should_throw_unable_to_deserialize_jwt() throws JsonProcessingException {
        // Given
        final JwtSecret jwtSecret = JwtSecret.builder().key(faker.cat().name()).issuer(faker.cat().breed()).type(
                "HS256").build();
        final HasuraJwtService hasuraJwtService = new HasuraJwtService(jwtSecret);
        final String jwtToken = JwtHelper.generateValidJwtFor(jwtSecret, faker.pokemon().name());

        // When
        final Optional<OnlyDustAuthentication> authentication =
                hasuraJwtService.getAuthenticationFromJwt(jwtToken);

        // Then
        assertThat(authentication).isNotPresent();
    }
}
