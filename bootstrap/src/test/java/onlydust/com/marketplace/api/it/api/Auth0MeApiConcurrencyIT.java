package onlydust.com.marketplace.api.it.api;

import com.auth0.jwt.interfaces.JWTVerifier;
import com.github.tomakehurst.wiremock.client.WireMock;
import jakarta.persistence.EntityManagerFactory;
import lombok.SneakyThrows;
import onlydust.com.marketplace.api.contract.model.GetMeResponse;
import onlydust.com.marketplace.api.helper.Auth0ApiClientStub;
import onlydust.com.marketplace.api.helper.ConcurrentTesting;
import onlydust.com.marketplace.api.helper.JwtVerifierStub;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.UserEntity;
import onlydust.com.marketplace.api.posthog.properties.PosthogProperties;
import onlydust.com.marketplace.api.suites.tags.TagUser;
import onlydust.com.marketplace.kernel.model.AuthenticatedUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;

import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder.okForEmptyJson;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static onlydust.com.marketplace.api.helper.ConcurrentTesting.runConcurrently;
import static org.assertj.core.api.Java6Assertions.assertThat;

@TagUser
public class Auth0MeApiConcurrencyIT extends AbstractMarketplaceApiIT {
    final static long TOKEN_EXPIRATION_IN_MILLISECONDS = 1000;

    Long githubUserId;
    String login;
    String avatarUrl;
    String email;
    String token;

    @Autowired
    JWTVerifier jwtVerifier;
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
                        WireMock.urlEqualTo("/api/v1/users/%d".formatted(githubUserId)))
                .withHeader("Content-Type", equalTo("application/json"))
                .withHeader("Api-Key", equalTo("some-indexer-api-key"))
                .willReturn(okForEmptyJson()));
    }


    @SneakyThrows
    @RepeatedTest(5)
    void should_sign_up_concurrently() {
        // Given
        final ConcurrentTesting.MutableObject<UUID> userId = new ConcurrentTesting.MutableObject<>();

        // When
        runConcurrently(50, threadId -> {
            final var me = client.get()
                    .uri(getApiURI(ME))
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .exchange()
                    .expectStatus().is2xxSuccessful()
                    .expectBody(GetMeResponse.class)
                    .returnResult().getResponseBody();
            assertThat(me).isNotNull();
            assertThat(userId.nullOrSetValue(me.getId())).isTrue();

            client.get()
                    .uri(getApiURI(ME_PROFILE))
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .exchange()
                    // Then
                    .expectStatus()
                    .is2xxSuccessful()
                    .expectBody()
                    .jsonPath("$.login").isEqualTo(login)
                    .jsonPath("$.githubUserId").isEqualTo(githubUserId)
                    .jsonPath("$.id").isEqualTo(me.getId().toString());
            assertMe(me);
            assertUserEntity(me.getId());
        });

        runJobs();
        indexerApiWireMockServer.verify(1, putRequestedFor(urlEqualTo("/api/v1/users/%d".formatted(githubUserId)))
                .withHeader("Content-Type", equalTo("application/json"))
        );
        posthogWireMockServer.verify(1, postRequestedFor(urlEqualTo("/capture/"))
                .withHeader("Content-Type", equalTo("application/json"))
                .withRequestBody(matchingJsonPath("$.api_key", equalTo(posthogProperties.getApiKey())))
                .withRequestBody(matchingJsonPath("$.event", equalTo("user_signed_up")))
                .withRequestBody(matchingJsonPath("$.distinct_id", equalTo(userId.getValue().toString())))
                .withRequestBody(matchingJsonPath("$.properties['$lib']", equalTo(posthogProperties.getUserAgent()))));
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

}
