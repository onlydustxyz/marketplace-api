package onlydust.com.marketplace.api.bootstrap.it.api;

import com.auth0.jwt.interfaces.JWTVerifier;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import lombok.SneakyThrows;
import onlydust.com.marketplace.api.bootstrap.helper.JwtVerifierStub;
import onlydust.com.marketplace.api.domain.model.UserRole;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.UserEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.OnboardingEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.UserRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.OnboardingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;

import java.util.Date;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

public class Auth0MeApiIT extends AbstractMarketplaceApiIT {
    final static String JWT_TOKEN = "fake-jwt";
    Long githubUserId;
    String login;
    String avatarUrl;

    @Autowired
    JWTVerifier jwtVerifier;
    @Autowired
    UserRepository userRepository;
    @Autowired
    OnboardingRepository onboardingRepository;

    @BeforeEach
    void setup() {
        githubUserId = faker.number().randomNumber();
        login = faker.name().username();
        avatarUrl = faker.internet().avatar();
        ((JwtVerifierStub) jwtVerifier).withJwtMock(JWT_TOKEN, githubUserId, login, avatarUrl);

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
                .jsonPath("$.hasSeenOnboardingWizard").isEqualTo(false)
                .jsonPath("$.hasAcceptedLatestTermsAndConditions").isEqualTo(false)
                .jsonPath("$.hasValidPayoutInfos").isEqualTo(true)
                .jsonPath("$.id").isNotEmpty();

        waitAtLeastOneCycleOfOutboxEventProcessing();
        indexerApiWireMockServer.verify(1, putRequestedFor(urlEqualTo("/api/v1/users/%d".formatted(githubUserId)))
                .withHeader("Content-Type", equalTo("application/json"))
        );


        // When we call it again (already signed-up)
        indexerApiWireMockServer.resetRequests();
        
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
                .jsonPath("$.hasSeenOnboardingWizard").isEqualTo(false)
                .jsonPath("$.hasAcceptedLatestTermsAndConditions").isEqualTo(false)
                .jsonPath("$.hasValidPayoutInfos").isEqualTo(true)
                .jsonPath("$.id").isNotEmpty();

        waitAtLeastOneCycleOfOutboxEventProcessing();
        indexerApiWireMockServer.verify(0, putRequestedFor(anyUrl()));
    }

    //    @Test
    void should_get_current_user_with_onboarding_data() {
        // Given
        final UserEntity user = UserEntity.builder()
                .id(UUID.randomUUID())
                .githubUserId(githubUserId)
                .githubLogin(login)
                .githubAvatarUrl(avatarUrl)
                .roles(new UserRole[]{UserRole.USER})
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
                .jsonPath("$.id").isNotEmpty();
    }
}
