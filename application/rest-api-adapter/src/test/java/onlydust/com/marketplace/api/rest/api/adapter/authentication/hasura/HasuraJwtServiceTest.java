package onlydust.com.marketplace.api.rest.api.adapter.authentication.hasura;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javafaker.Faker;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import onlydust.com.marketplace.api.domain.model.GithubUserIdentity;
import onlydust.com.marketplace.api.domain.model.User;
import onlydust.com.marketplace.api.domain.model.UserRole;
import onlydust.com.marketplace.api.domain.port.input.UserFacadePort;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.OnlyDustAuthentication;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.jwt.JwtSecret;
import org.junit.jupiter.api.Test;

public class HasuraJwtServiceTest {

  private final static Faker faker = new Faker();
  private static final ObjectMapper objectMapper = new ObjectMapper();

  private User mockUserFacadePort(final UserFacadePort userFacadePort, final boolean isAdmin) {
    final UUID userId = UUID.randomUUID();
    final Long userGithubId = faker.number().randomNumber();
    final String userLogin = faker.name().username();
    final String avatarUrl = faker.internet().avatar();

    final var user = User.builder()
        .id(userId)
        .githubLogin(userLogin)
        .githubAvatarUrl(avatarUrl)
        .githubUserId(userGithubId)
        .roles(isAdmin ? List.of(UserRole.ADMIN, UserRole.USER) : List.of(UserRole.USER))
        .hasSeenOnboardingWizard(true)
        .hasAcceptedLatestTermsAndConditions(true)
        .build();

    when(userFacadePort
        .getUserByGithubIdentity(GithubUserIdentity.builder()
            .githubUserId(userGithubId)
            .githubLogin(userLogin)
            .githubAvatarUrl(avatarUrl)
            .build(), false)
    ).thenReturn(user);

    return user;
  }

  @Test
  void should_authenticate_given_a_valid_jwt() throws JsonProcessingException {
    // Given
    final UserFacadePort userFacadePort = mock(UserFacadePort.class);
    final User user = mockUserFacadePort(userFacadePort, false);

    final JwtSecret jwtSecret = JwtSecret.builder().key(faker.cat().name()).issuer(faker.cat().breed()).type(
        "HS256").build();
    final HasuraJwtService hasuraJwtService = new HasuraJwtService(objectMapper, jwtSecret, userFacadePort);
    final HasuraJwtPayload hasuraJwtPayload =
        HasuraJwtPayload.builder()
            .iss(jwtSecret.getIssuer())
            .sub(faker.rickAndMorty().character())
            .claims(
                HasuraJwtPayload.HasuraClaims.builder()
                    .userId(user.getId())
                    .githubUserId(user.getGithubUserId())
                    .login(user.getGithubLogin())
                    .avatarUrl(user.getGithubAvatarUrl())
                    .allowedRoles(List.of("me", "registered_user", "public"))
                    .build()
            )
            .build();
    final String jwtToken = JwtHelper.generateValidJwtFor(jwtSecret, hasuraJwtPayload);

    // When
    final Optional<OnlyDustAuthentication> authentication = hasuraJwtService.getAuthenticationFromJwt(jwtToken,
        null);

    // Then
    assertThat(authentication).isPresent();
    final var authenticationFromJwt = authentication.get();
    assertTrue(authenticationFromJwt.isAuthenticated());
    assertThat(authenticationFromJwt.getName()).isEqualTo(hasuraJwtPayload.getClaims().getLogin());
    assertThat(authenticationFromJwt.getPrincipal()).isEqualTo(hasuraJwtPayload.getClaims().getLogin());
    assertThat(authenticationFromJwt.getCredentials()).isEqualTo(hasuraJwtPayload);
    assertThat(authenticationFromJwt.getUser().getId()).isEqualTo(hasuraJwtPayload.getClaims().getUserId());
    assertThat(authenticationFromJwt.getUser().getRoles()).containsExactlyInAnyOrder(UserRole.USER);
    assertThat(authenticationFromJwt.getUser().getGithubUserId()).isEqualTo(hasuraJwtPayload.getClaims().getGithubUserId());
    assertThat(authenticationFromJwt.getUser().hasSeenOnboardingWizard()).isTrue();
    assertThat(authenticationFromJwt.getUser().hasAcceptedLatestTermsAndConditions()).isTrue();
    assertThat(authenticationFromJwt.isImpersonating()).isFalse();
    assertThat(authenticationFromJwt.isImpersonating()).isFalse();
    assertThat(authenticationFromJwt.getImpersonator()).isNull();
    assertThat(((HasuraAuthentication) authenticationFromJwt).getJwt()).isEqualTo(jwtToken);
    assertThat(((HasuraAuthentication) authenticationFromJwt).getImpersonationHeader()).isNull();
  }


  @Test
  void should_throw_invalid_jwt_format_exception() {
    // Given
    final UserFacadePort userFacadePort = mock(UserFacadePort.class);
    final JwtSecret jwtSecret = JwtSecret.builder().key(faker.cat().name()).issuer(faker.cat().breed()).type(
        "HS256").build();
    final HasuraJwtService hasuraJwtService = new HasuraJwtService(objectMapper, jwtSecret, userFacadePort);

    // When
    final Optional<OnlyDustAuthentication> authentication =
        hasuraJwtService.getAuthenticationFromJwt(faker.chuckNorris().fact(), null);

    // Then
    assertThat(authentication).isNotPresent();
  }


  @Test
  void should_throw_invalid_header_format_exception() {
    // Given
    final UserFacadePort userFacadePort = mock(UserFacadePort.class);
    final User user = mockUserFacadePort(userFacadePort, false);
    final JwtSecret jwtSecret = JwtSecret.builder().key(faker.cat().name()).issuer(faker.cat().breed()).type(
        "HS256").build();
    final HasuraJwtService hasuraJwtService = new HasuraJwtService(objectMapper, jwtSecret, userFacadePort);
    final String jwtToken =
        faker.cat().name() + "." + faker.pokemon().name() + "." + faker.pokemon().name();

    // When
    final Optional<OnlyDustAuthentication> authentication = hasuraJwtService.getAuthenticationFromJwt(jwtToken,
        null);

    // Then
    assertThat(authentication).isNotPresent();
  }

  @Test
  void should_throw_unable_to_deserialize_jwt() throws JsonProcessingException {
    // Given
    final UserFacadePort userFacadePort = mock(UserFacadePort.class);
    final User user = mockUserFacadePort(userFacadePort, false);
    final JwtSecret jwtSecret = JwtSecret.builder().key(faker.cat().name()).issuer(faker.cat().breed()).type(
        "HS256").build();
    final HasuraJwtService hasuraJwtService = new HasuraJwtService(objectMapper, jwtSecret, userFacadePort);
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
    final UserFacadePort userFacadePort = mock(UserFacadePort.class);
    final User user = mockUserFacadePort(userFacadePort, true);
    final JwtSecret jwtSecret = JwtSecret.builder().key(faker.cat().name()).issuer(faker.cat().breed()).type(
        "HS256").build();
    final HasuraJwtService hasuraJwtService = new HasuraJwtService(objectMapper, jwtSecret, userFacadePort);
    final HasuraJwtPayload hasuraJwtPayload =
        HasuraJwtPayload.builder()
            .iss(jwtSecret.getIssuer())
            .sub(faker.rickAndMorty().character())
            .claims(
                HasuraJwtPayload.HasuraClaims.builder()
                    .userId(user.getId())
                    .githubUserId(user.getGithubUserId())
                    .login(user.getGithubLogin())
                    .avatarUrl(user.getGithubAvatarUrl())
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
    when(userFacadePort
        .getUserByGithubIdentity(GithubUserIdentity.builder()
            .githubUserId(595505L)
            .githubLogin("ofux")
            .githubAvatarUrl("https://avatars.githubusercontent.com/u/595505?v=4")
            .build(), false)
    ).thenReturn(User.builder()
        .id(UUID.fromString("50aa4318-141a-4027-8f74-c135d8d166b0"))
        .githubLogin("ofux")
        .githubAvatarUrl("https://avatars.githubusercontent.com/u/595505?v=4")
        .githubUserId(595505L)
        .roles(List.of(UserRole.USER))
        .hasSeenOnboardingWizard(false)
        .hasAcceptedLatestTermsAndConditions(true)
        .build());

    // When
    final Optional<OnlyDustAuthentication> authentication = hasuraJwtService.getAuthenticationFromJwt(jwtToken,
        impersonationHeader);

    // Then
    assertThat(authentication).isPresent();
    final var authenticationFromJwt = authentication.get();
    assertTrue(authenticationFromJwt.isAuthenticated());
    assertThat(authenticationFromJwt.getName()).isEqualTo("ofux");
    assertThat(authenticationFromJwt.getPrincipal()).isEqualTo("ofux");
    assertThat(authenticationFromJwt.getUser().getId().toString()).isEqualTo("50aa4318-141a-4027-8f74" +
        "-c135d8d166b0");
    assertThat(authenticationFromJwt.getUser().getRoles()).containsExactlyInAnyOrder(UserRole.USER);
    assertThat(authenticationFromJwt.getUser().getGithubUserId()).isEqualTo(595505L);
    assertThat(((HasuraAuthentication) authenticationFromJwt).getJwt()).isEqualTo(jwtToken);
    assertThat(((HasuraAuthentication) authenticationFromJwt).getImpersonationHeader()).isEqualTo(impersonationHeader);

    assertThat(authenticationFromJwt.isImpersonating()).isTrue();
    assertThat(authenticationFromJwt.getImpersonator()).isNotNull();
    final var impersonator = authenticationFromJwt.getImpersonator();
    assertThat(impersonator.getId()).isEqualTo(hasuraJwtPayload.getClaims().getUserId());
    assertThat(impersonator.getRoles()).containsExactlyInAnyOrder(UserRole.ADMIN, UserRole.USER);
    assertThat(impersonator.getGithubUserId()).isEqualTo(hasuraJwtPayload.getClaims().getGithubUserId());
  }

  @Test
  void should_reject_impersonation_when_impersonator_is_not_admin() throws JsonProcessingException {
    // Given
    final UserFacadePort userFacadePort = mock(UserFacadePort.class);
    final User user = mockUserFacadePort(userFacadePort, false);
    final JwtSecret jwtSecret = JwtSecret.builder().key(faker.cat().name()).issuer(faker.cat().breed()).type(
        "HS256").build();
    final HasuraJwtService hasuraJwtService = new HasuraJwtService(objectMapper, jwtSecret, userFacadePort);
    final HasuraJwtPayload hasuraJwtPayload =
        HasuraJwtPayload.builder()
            .iss(jwtSecret.getIssuer())
            .sub(faker.rickAndMorty().character())
            .claims(
                HasuraJwtPayload.HasuraClaims.builder()
                    .userId(user.getId())
                    .githubUserId(user.getGithubUserId())
                    .login(user.getGithubLogin())
                    .avatarUrl(user.getGithubAvatarUrl())
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
    when(userFacadePort
        .getUserByGithubIdentity(GithubUserIdentity.builder()
            .githubUserId(595505L)
            .githubLogin("foo")
            .githubAvatarUrl("https://avatars.githubusercontent.com/u/595505?v=4")
            .build(), false)
    ).thenReturn(User.builder()
        .id(UUID.fromString("50aa4318-141a-4027-8f74-c135d8d166b0"))
        .githubLogin("foo")
        .githubAvatarUrl("https://avatars.githubusercontent.com/u/595505?v=4")
        .githubUserId(595505L)
        .roles(List.of(UserRole.USER))
        .hasSeenOnboardingWizard(true)
        .hasAcceptedLatestTermsAndConditions(false)
        .build());

    // When
    final Optional<OnlyDustAuthentication> authentication = hasuraJwtService.getAuthenticationFromJwt(jwtToken,
        impersonationHeader);

    // Then
    assertThat(authentication).isNotPresent();
  }
}
