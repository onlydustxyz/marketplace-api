package onlydust.com.marketplace.api.rest.api.adapter.authentication.hasura;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.javafaker.Faker;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.OnlyDustAuthentication;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.jwt.JwtSecret;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
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
                                        .login(faker.name().username())
                                        .allowedRoles(List.of("me", "registered_user", "public"))
                                        .build()
                        )
                        .build();
        final String jwtToken = JwtHelper.generateValidJwtFor(jwtSecret, hasuraJwtPayload);

        // When
        final Optional<OnlyDustAuthentication> authentication = hasuraJwtService.getAuthenticationFromJwt(jwtToken, null);

        // Then
        assertThat(authentication).isPresent();
        final var authenticationFromJwt = authentication.get();
        assertTrue(authenticationFromJwt.isAuthenticated());
        assertThat(authenticationFromJwt.getName()).isEqualTo(hasuraJwtPayload.getClaims().getLogin());
        assertThat(authenticationFromJwt.getPrincipal()).isEqualTo(hasuraJwtPayload.getClaims().getLogin());
        assertThat(authenticationFromJwt.getCredentials()).isEqualTo(hasuraJwtPayload);
        assertThat(authenticationFromJwt.getUser().getId()).isEqualTo(hasuraJwtPayload.getClaims().getUserId());
        assertThat(authenticationFromJwt.getUser().getPermissions()).isEqualTo(hasuraJwtPayload.getClaims().getAllowedRoles());
        assertThat(authenticationFromJwt.getUser().getGithubUserId()).isEqualTo(hasuraJwtPayload.getClaims().getGithubUserId());
        assertThat(authenticationFromJwt.isImpersonating()).isFalse();
        assertThat(authenticationFromJwt.getImpersonator()).isNull();
    }


    @Test
    void should_throw_invalid_jwt_format_exception() {
        // Given
        final JwtSecret jwtSecret = JwtSecret.builder().key(faker.cat().name()).issuer(faker.cat().breed()).type(
                "HS256").build();
        final HasuraJwtService hasuraJwtService = new HasuraJwtService(jwtSecret);

        // When
        final Optional<OnlyDustAuthentication> authentication =
                hasuraJwtService.getAuthenticationFromJwt(faker.chuckNorris().fact(), null);

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
        final Optional<OnlyDustAuthentication> authentication = hasuraJwtService.getAuthenticationFromJwt(jwtToken, null);

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
                hasuraJwtService.getAuthenticationFromJwt(jwtToken, null);

        // Then
        assertThat(authentication).isNotPresent();
    }

    @Test
    void should_authenticate_given_a_valid_jwt_and_impersonation_header() throws JsonProcessingException {
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
                                        .login(faker.name().username())
                                        .isAnOnlydustAdmin(true)
                                        .allowedRoles(List.of("me", "registered_user", "public"))
                                        .build()
                        )
                        .build();
        final String jwtToken = JwtHelper.generateValidJwtFor(jwtSecret, hasuraJwtPayload);

        final String impersonationHeader = """
                {
                    "x-hasura-projectsLeaded": "{}",
                    "x-hasura-githubUserId": "595505",
                    "x-hasura-odAdmin": "false",
                    "x-hasura-githubAccessToken": "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",
                    "x-hasura-allowed-roles": [
                      "me",
                      "registered_user",
                      "public"
                    ],
                    "x-hasura-default-role": "registered_user",
                    "x-hasura-user-id": "50aa4318-141a-4027-8f74-c135d8d166b0",
                    "x-hasura-user-is-anonymous": "false",
                    "x-hasura-login": "ofux",
                    "x-hasura-avatarUrl": "https://avatars.githubusercontent.com/u/595505?v=4"
                }
                """;

        // When
        final Optional<OnlyDustAuthentication> authentication = hasuraJwtService.getAuthenticationFromJwt(jwtToken, impersonationHeader);

        // Then
        assertThat(authentication).isPresent();
        final var authenticationFromJwt = authentication.get();
        assertTrue(authenticationFromJwt.isAuthenticated());
        assertThat(authenticationFromJwt.getName()).isEqualTo("ofux");
        assertThat(authenticationFromJwt.getPrincipal()).isEqualTo("ofux");
        assertThat(authenticationFromJwt.getUser().getId().toString()).isEqualTo("50aa4318-141a-4027-8f74-c135d8d166b0");
        assertThat(authenticationFromJwt.getUser().getPermissions()).containsExactlyInAnyOrder("me", "registered_user", "public");
        assertThat(authenticationFromJwt.getUser().getGithubUserId()).isEqualTo(595505L);

        assertThat(authenticationFromJwt.isImpersonating()).isTrue();
        assertThat(authenticationFromJwt.getImpersonator()).isNotNull();
        final var impersonator = authenticationFromJwt.getImpersonator();
        assertThat(impersonator.getId()).isEqualTo(hasuraJwtPayload.getClaims().getUserId());
        assertThat(impersonator.getPermissions()).containsExactlyInAnyOrder("me", "registered_user", "public", "impersonation");
        assertThat(impersonator.getGithubUserId()).isEqualTo(hasuraJwtPayload.getClaims().getGithubUserId());
    }

    @Test
    void should_reject_impersonation_when_impersonator_is_not_admin() throws JsonProcessingException {
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
                                        .login(faker.name().username())
                                        .isAnOnlydustAdmin(false)
                                        .allowedRoles(List.of("me", "registered_user", "public"))
                                        .build()
                        )
                        .build();
        final String jwtToken = JwtHelper.generateValidJwtFor(jwtSecret, hasuraJwtPayload);

        final String impersonationHeader = """
                {
                    "x-hasura-projectsLeaded": "{}",
                    "x-hasura-githubUserId": "595505",
                    "x-hasura-odAdmin": "false",
                    "x-hasura-githubAccessToken": "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",
                    "x-hasura-allowed-roles": [
                      "me",
                      "registered_user",
                      "public"
                    ],
                    "x-hasura-default-role": "registered_user",
                    "x-hasura-user-id": "50aa4318-141a-4027-8f74-c135d8d166b0",
                    "x-hasura-user-is-anonymous": "false",
                    "x-hasura-login": "foo",
                    "x-hasura-avatarUrl": "https://avatars.githubusercontent.com/u/595505?v=4"
                }
                """;

        // When
        final Optional<OnlyDustAuthentication> authentication = hasuraJwtService.getAuthenticationFromJwt(jwtToken, impersonationHeader);

        // Then
        assertThat(authentication).isNotPresent();
    }
}
