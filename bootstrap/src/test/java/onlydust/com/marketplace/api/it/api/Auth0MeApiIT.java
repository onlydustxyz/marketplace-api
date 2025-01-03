package onlydust.com.marketplace.api.it.api;

import com.auth0.jwt.interfaces.JWTVerifier;
import com.github.tomakehurst.wiremock.client.WireMock;
import jakarta.persistence.EntityManagerFactory;
import lombok.SneakyThrows;
import onlydust.com.marketplace.api.contract.model.GetMeResponse;
import onlydust.com.marketplace.api.helper.Auth0ApiClientStub;
import onlydust.com.marketplace.api.helper.JwtVerifierStub;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.UserEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.OnboardingEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.UserRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.OnboardingRepository;
import onlydust.com.marketplace.api.posthog.properties.PosthogProperties;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticationFilter;
import onlydust.com.marketplace.api.suites.tags.TagUser;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import onlydust.com.marketplace.kernel.model.AuthenticatedUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;

import java.util.Date;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder.okForEmptyJson;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.assertj.core.api.Java6Assertions.assertThatThrownBy;

@TagUser
public class Auth0MeApiIT extends AbstractMarketplaceApiIT {
    final static long TOKEN_EXPIRATION_IN_MILLISECONDS = 1000;

    Long githubUserId;
    String login;
    String avatarUrl;
    String email;
    String token;

    @Autowired
    JWTVerifier jwtVerifier;
    @Autowired
    UserRepository userRepository;
    @Autowired
    OnboardingRepository onboardingRepository;
    @Autowired
    EntityManagerFactory entityManagerFactory;
    @Autowired
    PosthogProperties posthogProperties;
    @Autowired
    Auth0ApiClientStub auth0ApiClientStub;

    @BeforeEach
    void setup() {
        githubUserId = faker.number().randomNumber(15, true);
        login = faker.name().username();
        avatarUrl = faker.internet().avatar();
        email = faker.internet().emailAddress();
        token = ((JwtVerifierStub) jwtVerifier).tokenFor(githubUserId, TOKEN_EXPIRATION_IN_MILLISECONDS);

        userAuthHelper.mockAuth0UserInfo(githubUserId, login, login, avatarUrl, email);
        auth0ApiClientStub.withPat(githubUserId, token);

        githubWireMockServer.stubFor(WireMock.get(urlEqualTo("/"))
                .withHeader("Authorization", equalTo("Bearer %s".formatted(token)))
                .willReturn(okForEmptyJson().withHeader("x-oauth-scopes", "read:org, read:packages, public_repo")));

        indexerApiWireMockServer.stubFor(WireMock.put(
                        WireMock.urlEqualTo("/api/v1/users/%d?forceRefresh=true".formatted(githubUserId)))
                .withHeader("Content-Type", equalTo("application/json"))
                .withHeader("Api-Key", equalTo("some-indexer-api-key"))
                .willReturn(okForEmptyJson()));
    }

    @Test
    public void should_be_unauthorized() {
        // When
        client.get()
                .uri(getApiURI(ME))
                // Then
                .exchange()
                .expectStatus()
                .isEqualTo(401);
    }


    @SneakyThrows
    @Test
    void should_sign_up_and_get_user_given_a_valid_jwt() {
        // Given

        // When
        var me = client.get()
                .uri(getApiURI(ME))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody(GetMeResponse.class)
                .returnResult().getResponseBody();

        // Then
        client.get()
                .uri(getApiURI(ME_PROFILE))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody().jsonPath("$.login").isEqualTo(login);
        assertMe(me);
        assertUserEntity(me.getId());
        runJobs();
        indexerApiWireMockServer.verify(1, putRequestedFor(urlEqualTo("/api/v1/users/%d?forceRefresh=true".formatted(githubUserId)))
                .withHeader("Content-Type", equalTo("application/json"))
        );
        posthogWireMockServer.verify(1, postRequestedFor(urlEqualTo("/capture/"))
                .withHeader("Content-Type", equalTo("application/json"))
                .withRequestBody(matchingJsonPath("$.api_key", equalTo(posthogProperties.getApiKey())))
                .withRequestBody(matchingJsonPath("$.event", equalTo("user_signed_up")))
                .withRequestBody(matchingJsonPath("$.distinct_id", equalTo(me.getId().toString())))
                .withRequestBody(matchingJsonPath("$.properties['$lib']", equalTo(posthogProperties.getUserAgent()))));

        // ===============================================
        // When we call it again (already signed-up)
        indexerApiWireMockServer.resetRequests();

        me = client.get()
                .uri(getApiURI(ME))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody(GetMeResponse.class)
                .returnResult().getResponseBody();

        // Then
        assertMe(me);
        assertUserEntity(me.getId());
        runJobs();
        indexerApiWireMockServer.verify(0, putRequestedFor(anyUrl()));

        // ===============================================
        // When we call it again after a logout
        client.post()
                .uri(getApiURI(ME_LOGOUT))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .exchange()
                .expectStatus().isNoContent();

        client.get()
                .uri(getApiURI(ME))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .exchange()
                .expectStatus()
                .is5xxServerError();

        assertThatThrownBy(() -> auth0ApiClientStub.getGithubPersonalToken(githubUserId))
                .isInstanceOf(OnlyDustException.class)
                .hasMessage("No Github personal token for user %d in Auth0ApiClientStub".formatted(githubUserId));

        // ===============================================
        // When we call it again (already signed-up) with a new email
        indexerApiWireMockServer.resetRequests();
        Thread.sleep(TOKEN_EXPIRATION_IN_MILLISECONDS + 10); // make sure user claims won't be in cache anymore
        token = ((JwtVerifierStub) jwtVerifier).tokenFor(githubUserId);
        auth0ApiClientStub.withPat(githubUserId, token);

        userAuthHelper.mockAuth0UserInfo(githubUserId, login, login, avatarUrl, faker.internet().emailAddress());

        me = client.get()
                .uri(getApiURI(ME))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody(GetMeResponse.class)
                .returnResult().getResponseBody();

        // Then
        assertMe(me);
        runJobs();
        indexerApiWireMockServer.verify(0, putRequestedFor(anyUrl()));
    }

    private void runJobs() {
        indexerOutboxJob.run();
        trackingOutboxJob.run();
    }

    protected void assertUserEntity(UUID userId) {
        final var em = entityManagerFactory.createEntityManager();
        final var userEntity = em.find(UserEntity.class, userId);
        assertThat(userEntity).isNotNull();
        assertThat(userEntity.getGithubUserId()).isEqualTo(githubUserId);
        assertThat(userEntity.getGithubLogin()).isEqualTo(login);
        assertThat(userEntity.getGithubAvatarUrl()).isEqualTo(avatarUrl);
        assertThat(userEntity.getEmail()).isEqualTo(email);
        assertThat(userEntity.getRoles()).containsExactly(AuthenticatedUser.Role.USER);
        em.close();
    }

    private void assertMe(GetMeResponse me) {
        assertThat(me).isNotNull();
        assertThat(me.getLogin()).isEqualTo(login);
        assertThat(me.getGithubUserId()).isEqualTo(githubUserId);
        assertThat(me.getAvatarUrl()).isEqualTo(avatarUrl);
        assertThat(me.getEmail()).isEqualTo(email);
        assertThat(me.getHasCompletedOnboarding()).isEqualTo(false);
        assertThat(me.getHasAcceptedLatestTermsAndConditions()).isEqualTo(false);
        assertThat(me.getIsAdmin()).isEqualTo(false);
        assertThat(me.getId()).isNotNull();
        assertThat(me.getIsAuthorizedToApplyOnGithubIssues()).isTrue();
    }

    @Test
    void should_get_current_user_with_onboarding_data() {
        // Given
        final Date createdAt = new Date();
        final UserEntity user = UserEntity.builder()
                .id(UUID.randomUUID())
                .githubUserId(githubUserId)
                .githubLogin(login)
                .githubAvatarUrl(avatarUrl)
                .email(email)
                .lastSeenAt(new Date())
                .createdAt(createdAt)
                .roles(new AuthenticatedUser.Role[]{AuthenticatedUser.Role.USER, AuthenticatedUser.Role.ADMIN})
                .build();
        userRepository.save(user);

        final OnboardingEntity onboarding = OnboardingEntity.builder()
                .userId(user.getId())
                .termsAndConditionsAcceptanceDate(new Date())
                .completionDate(new Date())
                .build();
        onboardingRepository.save(onboarding);

        // When
        client.get()
                .uri(getApiURI(ME))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .exchange()
                // Then
                .expectStatus().is2xxSuccessful()
                .expectBody()
                .jsonPath("$.login").isEqualTo(login)
                .jsonPath("$.githubUserId").isEqualTo(githubUserId)
                .jsonPath("$.avatarUrl").isEqualTo(avatarUrl)
                .jsonPath("$.hasCompletedOnboarding").isEqualTo(true)
                .jsonPath("$.isAdmin").isEqualTo(true)
                .jsonPath("$.createdAt").isNotEmpty()
                .jsonPath("$.id").isEqualTo(user.getId().toString());
    }

    @Test
    void should_get_impersonated_user() {
        // Given
        final UserEntity impersonatorUser = UserEntity.builder()
                .id(UUID.randomUUID())
                .githubUserId(githubUserId)
                .githubLogin(login)
                .githubAvatarUrl(avatarUrl)
                .email(email)
                .lastSeenAt(new Date())
                .roles(new AuthenticatedUser.Role[]{AuthenticatedUser.Role.USER, AuthenticatedUser.Role.ADMIN})
                .build();
        userRepository.save(impersonatorUser);

        long impresonatedGithubUserId = githubUserId + faker.number().numberBetween(1, 1000);
        final UserEntity impersonatedUser = UserEntity.builder()
                .id(UUID.randomUUID())
                .githubUserId(impresonatedGithubUserId)
                .githubLogin(faker.name().username())
                .githubAvatarUrl(faker.internet().avatar())
                .email(faker.internet().emailAddress())
                .lastSeenAt(new Date())
                .roles(new AuthenticatedUser.Role[]{AuthenticatedUser.Role.USER})
                .build();
        userRepository.save(impersonatedUser);
        auth0ApiClientStub.withPat(impresonatedGithubUserId, token);

        // When
        client.get()
                .uri(getApiURI(ME))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .header(AuthenticationFilter.IMPERSONATION_HEADER,
                        "{\"sub\":\"github|%d\"}".formatted(impersonatedUser.getGithubUserId())
                )
                .exchange()
                // Then
                .expectStatus().is2xxSuccessful()
                .expectBody()
                .jsonPath("$.login").isEqualTo(impersonatedUser.getGithubLogin())
                .jsonPath("$.githubUserId").isEqualTo(impersonatedUser.getGithubUserId())
                .jsonPath("$.avatarUrl").isEqualTo(impersonatedUser.getGithubAvatarUrl())
                // TODO : maybe check if we get the BP of the impersonated user
                .jsonPath("$.hasCompletedOnboarding").isEqualTo(false)
                .jsonPath("$.isAdmin").isEqualTo(false)
                .jsonPath("$.id").isEqualTo(impersonatedUser.getId().toString());
    }

    @Test
    void should_fail_to_impersonate_non_registered_user() {
        // Given
        final UserEntity impersonatorUser = UserEntity.builder()
                .id(UUID.randomUUID())
                .githubUserId(githubUserId)
                .githubLogin(login)
                .githubAvatarUrl(avatarUrl)
                .email(email)
                .lastSeenAt(new Date())
                .roles(new AuthenticatedUser.Role[]{AuthenticatedUser.Role.USER, AuthenticatedUser.Role.ADMIN})
                .build();
        userRepository.save(impersonatorUser);

        final long impersonatedUserId = githubUserId + faker.number().numberBetween(1, 1000);

        // When
        client.get()
                .uri(getApiURI(ME))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .header(AuthenticationFilter.IMPERSONATION_HEADER,
                        "{\"sub\":\"github|%d\"}".formatted(impersonatedUserId)
                )
                .exchange()
                // Then
                .expectStatus().isUnauthorized();
    }

    @Test
    void should_fail_to_impersonate_user_when_not_admin() {
        // Given
        final UserEntity impersonatorUser = UserEntity.builder()
                .id(UUID.randomUUID())
                .githubUserId(githubUserId)
                .githubLogin(login)
                .githubAvatarUrl(avatarUrl)
                .email(email)
                .lastSeenAt(new Date())
                .roles(new AuthenticatedUser.Role[]{AuthenticatedUser.Role.USER})
                .build();
        userRepository.save(impersonatorUser);

        final UserEntity impersonatedUser = UserEntity.builder()
                .id(UUID.randomUUID())
                .githubUserId(githubUserId + faker.number().numberBetween(1, 1000))
                .githubLogin(faker.name().username())
                .githubAvatarUrl(faker.internet().avatar())
                .email(faker.internet().emailAddress())
                .lastSeenAt(new Date())
                .roles(new AuthenticatedUser.Role[]{AuthenticatedUser.Role.USER})
                .build();
        userRepository.save(impersonatedUser);

        // When
        client.get()
                .uri(getApiURI(ME))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .header(AuthenticationFilter.IMPERSONATION_HEADER,
                        "{\"sub\":\"github|%d\"}".formatted(impersonatedUser.getGithubUserId())
                )
                .exchange()
                // Then
                .expectStatus().isUnauthorized();
    }
}
