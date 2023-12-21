package onlydust.com.marketplace.api.bootstrap.it.api;

import com.auth0.jwt.interfaces.JWTVerifier;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import lombok.SneakyThrows;
import onlydust.com.marketplace.api.bootstrap.helper.JwtVerifierStub;
import onlydust.com.marketplace.api.contract.model.GetMeResponse;
import onlydust.com.marketplace.api.domain.model.UserRole;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.UserEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.OnboardingEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.UserRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.OnboardingRepository;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticationFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;

import javax.persistence.EntityManagerFactory;
import java.util.Date;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Java6Assertions.assertThat;

public class Auth0MeApiIT extends AbstractMarketplaceApiIT {
    final static String JWT_TOKEN = "fake-jwt";
    Long githubUserId;
    String login;
    String avatarUrl;
    String email;

    @Autowired
    JWTVerifier jwtVerifier;
    @Autowired
    UserRepository userRepository;
    @Autowired
    OnboardingRepository onboardingRepository;
    @Autowired
    EntityManagerFactory entityManagerFactory;

    @BeforeEach
    void setup() {
        githubUserId = faker.number().randomNumber();
        login = faker.name().username();
        avatarUrl = faker.internet().avatar();
        email = faker.internet().emailAddress();
        ((JwtVerifierStub) jwtVerifier).withJwtMock(JWT_TOKEN, githubUserId, login, avatarUrl, email);

        indexerApiWireMockServer.stubFor(WireMock.put(
                        WireMock.urlEqualTo("/api/v1/users/%d".formatted(githubUserId)))
                .withHeader("Content-Type", equalTo("application/json"))
                .withHeader("Api-Key", equalTo("some-indexer-api-key"))
                .willReturn(ResponseDefinitionBuilder.okForEmptyJson()));
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


    @SneakyThrows
    @Test
    void should_sign_up_and_get_user_given_a_valid_jwt() {
        // Given

        // When
        var me = client.get()
                .uri(getApiURI(ME_GET))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + JWT_TOKEN)
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody(GetMeResponse.class)
                .returnResult().getResponseBody();

        // Then
        assertMe(me);
        assertUserEntity(me.getId());
        waitAtLeastOneCycleOfOutboxEventProcessing();
        indexerApiWireMockServer.verify(1, putRequestedFor(urlEqualTo("/api/v1/users/%d".formatted(githubUserId)))
                .withHeader("Content-Type", equalTo("application/json"))
        );

        // ===============================================
        // When we call it again (already signed-up)
        indexerApiWireMockServer.resetRequests();

        me = client.get()
                .uri(getApiURI(ME_GET))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + JWT_TOKEN)
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody(GetMeResponse.class)
                .returnResult().getResponseBody();

        // Then
        assertMe(me);
        assertUserEntity(me.getId());
        waitAtLeastOneCycleOfOutboxEventProcessing();
        indexerApiWireMockServer.verify(0, putRequestedFor(anyUrl()));

        // ===============================================
        // When we call it again (already signed-up) with a new email
        indexerApiWireMockServer.resetRequests();
        email = faker.internet().emailAddress();
        ((JwtVerifierStub) jwtVerifier).withJwtMock(JWT_TOKEN, githubUserId, login, avatarUrl, email);

        me = client.get()
                .uri(getApiURI(ME_GET))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + JWT_TOKEN)
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody(GetMeResponse.class)
                .returnResult().getResponseBody();

        // Then
        assertMe(me);
        assertUserEntity(me.getId());
        waitAtLeastOneCycleOfOutboxEventProcessing();
        indexerApiWireMockServer.verify(0, putRequestedFor(anyUrl()));
    }

    protected void assertUserEntity(UUID userId) {
        final var userEntity = entityManagerFactory.createEntityManager().find(UserEntity.class, userId);
        assertThat(userEntity).isNotNull();
        assertThat(userEntity.getGithubUserId()).isEqualTo(githubUserId);
        assertThat(userEntity.getGithubLogin()).isEqualTo(login);
        assertThat(userEntity.getGithubAvatarUrl()).isEqualTo(avatarUrl);
        assertThat(userEntity.getGithubEmail()).isEqualTo(email);
        assertThat(userEntity.getRoles()).containsExactly(UserRole.USER);
    }

    private void assertMe(GetMeResponse me) {
        assertThat(me).isNotNull();
        assertThat(me.getLogin()).isEqualTo(login);
        assertThat(me.getGithubUserId()).isEqualTo(githubUserId);
        assertThat(me.getAvatarUrl()).isEqualTo(avatarUrl);
        assertThat(me.getHasSeenOnboardingWizard()).isEqualTo(false);
        assertThat(me.getHasAcceptedLatestTermsAndConditions()).isEqualTo(false);
        assertThat(me.getHasValidPayoutInfos()).isEqualTo(true);
        assertThat(me.getIsAdmin()).isEqualTo(false);
        assertThat(me.getId()).isNotNull();
    }

    @Test
    void should_get_current_user_with_onboarding_data() {
        // Given
        final UserEntity user = UserEntity.builder()
                .id(UUID.randomUUID())
                .githubUserId(githubUserId)
                .githubLogin(login)
                .githubAvatarUrl(avatarUrl)
                .githubEmail(email)
                .lastSeenAt(new Date())
                .roles(new UserRole[]{UserRole.USER, UserRole.ADMIN})
                .build();
        userRepository.save(user);

        final OnboardingEntity onboarding = OnboardingEntity.builder()
                .id(user.getId())
                .termsAndConditionsAcceptanceDate(new Date())
                .profileWizardDisplayDate(new Date())
                .build();
        onboardingRepository.save(onboarding);

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
                .jsonPath("$.hasSeenOnboardingWizard").isEqualTo(true)
                .jsonPath("$.hasAcceptedLatestTermsAndConditions").isEqualTo(true)
                .jsonPath("$.hasValidPayoutInfos").isEqualTo(true)
                .jsonPath("$.isAdmin").isEqualTo(true)
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
                .githubEmail(email)
                .lastSeenAt(new Date())
                .roles(new UserRole[]{UserRole.USER, UserRole.ADMIN})
                .build();
        userRepository.save(impersonatorUser);

        final UserEntity impersonatedUser = UserEntity.builder()
                .id(UUID.randomUUID())
                .githubUserId(githubUserId + faker.number().numberBetween(1, 1000))
                .githubLogin(faker.name().username())
                .githubAvatarUrl(faker.internet().avatar())
                .githubEmail(faker.internet().emailAddress())
                .lastSeenAt(new Date())
                .roles(new UserRole[]{UserRole.USER})
                .build();
        userRepository.save(impersonatedUser);

        // When
        client.get()
                .uri(getApiURI(ME_GET))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + JWT_TOKEN)
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
                .jsonPath("$.hasSeenOnboardingWizard").isEqualTo(false)
                .jsonPath("$.hasAcceptedLatestTermsAndConditions").isEqualTo(false)
                .jsonPath("$.hasValidPayoutInfos").isEqualTo(true)
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
                .githubEmail(email)
                .lastSeenAt(new Date())
                .roles(new UserRole[]{UserRole.USER, UserRole.ADMIN})
                .build();
        userRepository.save(impersonatorUser);

        final long impersonatedUserId = githubUserId + faker.number().numberBetween(1, 1000);

        // When
        client.get()
                .uri(getApiURI(ME_GET))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + JWT_TOKEN)
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
                .githubEmail(email)
                .lastSeenAt(new Date())
                .roles(new UserRole[]{UserRole.USER})
                .build();
        userRepository.save(impersonatorUser);

        final UserEntity impersonatedUser = UserEntity.builder()
                .id(UUID.randomUUID())
                .githubUserId(githubUserId + faker.number().numberBetween(1, 1000))
                .githubLogin(faker.name().username())
                .githubAvatarUrl(faker.internet().avatar())
                .githubEmail(faker.internet().emailAddress())
                .lastSeenAt(new Date())
                .roles(new UserRole[]{UserRole.USER})
                .build();
        userRepository.save(impersonatedUser);

        // When
        client.get()
                .uri(getApiURI(ME_GET))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + JWT_TOKEN)
                .header(AuthenticationFilter.IMPERSONATION_HEADER,
                        "{\"sub\":\"github|%d\"}".formatted(impersonatedUser.getGithubUserId())
                )
                .exchange()
                // Then
                .expectStatus().isUnauthorized();
    }
}
