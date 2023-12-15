package onlydust.com.marketplace.api.bootstrap.it.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import onlydust.com.marketplace.api.bootstrap.helper.HasuraJwtHelper;
import onlydust.com.marketplace.api.domain.model.UserRole;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.AuthUserEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.OnboardingEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.UserRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.AuthUserRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.OnboardingRepository;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.hasura.HasuraJwtPayload;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.jwt.JwtSecret;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;


@ActiveProfiles({"hasura_auth"})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class HasuraAuthMeApiIT extends AbstractMarketplaceApiIT {

    @Autowired
    JwtSecret jwtSecret;
    @Autowired
    AuthUserRepository authUserRepository;
    @Autowired
    OnboardingRepository onboardingRepository;
    @Autowired
    UserRepository userRepository;

    @Test
    @Order(1)
    public void should_be_unauthorized() {
        // When
        client.get()
                .uri(getApiURI(ME_GET))
                // Then
                .exchange()
                .expectStatus()
                .isEqualTo(401);
    }

    @Test
    @Order(2)
    void should_get_current_user_given_a_valid_jwt() throws JsonProcessingException {
        // Given
        Long githubUserId = faker.number().randomNumber();
        String login = faker.name().username();
        String avatarUrl = faker.internet().avatar();
        String email = faker.internet().emailAddress();
        UUID userId = UUID.randomUUID();

        final AuthUserEntity user = AuthUserEntity.builder()
                .id(userId)
                .githubUserId(githubUserId)
                .loginAtSignup(login)
                .avatarUrlAtSignup(avatarUrl)
                .email(email)
                .isAdmin(false)
                .createdAt(new Date())
                .build();
        authUserRepository.save(user);

        final var iamUser = userRepository.findById(userId);
        assertThat(iamUser).isPresent();
        assertThat(iamUser.get().getId()).isEqualTo(userId);
        assertThat(iamUser.get().getGithubUserId()).isEqualTo(githubUserId);
        assertThat(iamUser.get().getGithubLogin()).isEqualTo(login);
        assertThat(iamUser.get().getGithubAvatarUrl()).isEqualTo(avatarUrl);
        assertThat(iamUser.get().getEmail()).isEqualTo(email);
        assertThat(iamUser.get().getRoles()).containsExactly(UserRole.USER);

        final String jwt = HasuraJwtHelper.generateValidJwtFor(jwtSecret, HasuraJwtPayload.builder()
                .iss(jwtSecret.getIssuer())
                .claims(HasuraJwtPayload.HasuraClaims.builder()
                        .userId(userId)
                        .allowedRoles(List.of("me"))
                        .githubUserId(githubUserId)
                        .avatarUrl(avatarUrl)
                        .login(login)
                        .build())
                .build());

        // When
        client.get()
                .uri(getApiURI(ME_GET))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
                .exchange()
                // Then
                .expectStatus().is2xxSuccessful()
                .expectBody()
                .jsonPath("$.login").isEqualTo(login)
                .jsonPath("$.githubUserId").isEqualTo(githubUserId)
                .jsonPath("$.avatarUrl").isEqualTo(avatarUrl)
                .jsonPath("$.hasSeenOnboardingWizard").isEqualTo(false)
                .jsonPath("$.hasAcceptedLatestTermsAndConditions").isEqualTo(false)
                .jsonPath("$.id").isEqualTo(userId.toString());
    }

    @Test
    @Order(3)
    void should_get_current_user_with_onboarding_data() throws JsonProcessingException {
        // Given
        Long githubUserId = faker.number().randomNumber();
        String login = faker.name().username();
        String avatarUrl = faker.internet().avatar();
        String email = faker.internet().emailAddress();
        UUID userId = UUID.randomUUID();

        final AuthUserEntity user = AuthUserEntity.builder()
                .id(userId)
                .githubUserId(githubUserId)
                .loginAtSignup(login)
                .avatarUrlAtSignup(avatarUrl)
                .email(email)
                .isAdmin(false)
                .createdAt(new Date())
                .build();
        authUserRepository.save(user);

        final var iamUser = userRepository.findById(userId);
        assertThat(iamUser).isPresent();
        assertThat(iamUser.get().getId()).isEqualTo(userId);
        assertThat(iamUser.get().getGithubUserId()).isEqualTo(githubUserId);
        assertThat(iamUser.get().getGithubLogin()).isEqualTo(login);
        assertThat(iamUser.get().getGithubAvatarUrl()).isEqualTo(avatarUrl);
        assertThat(iamUser.get().getEmail()).isEqualTo(email);
        assertThat(iamUser.get().getRoles()).containsExactly(UserRole.USER);

        final OnboardingEntity onboarding = OnboardingEntity.builder()
                .id(user.getId())
                .termsAndConditionsAcceptanceDate(new Date())
                .profileWizardDisplayDate(new Date())
                .build();
        onboardingRepository.save(onboarding);

        final String jwt = HasuraJwtHelper.generateValidJwtFor(jwtSecret, HasuraJwtPayload.builder()
                .iss(jwtSecret.getIssuer())
                .claims(HasuraJwtPayload.HasuraClaims.builder()
                        .userId(userId)
                        .allowedRoles(List.of("me"))
                        .githubUserId(githubUserId)
                        .avatarUrl(avatarUrl)
                        .login(login)
                        .build())
                .build());

        // When
        client.get()
                .uri(getApiURI(ME_GET))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
                .exchange()
                // Then
                .expectStatus().is2xxSuccessful()
                .expectBody()
                .jsonPath("$.login").isEqualTo(login)
                .jsonPath("$.githubUserId").isEqualTo(githubUserId)
                .jsonPath("$.avatarUrl").isEqualTo(avatarUrl)
                .jsonPath("$.hasSeenOnboardingWizard").isEqualTo(true)
                .jsonPath("$.hasAcceptedLatestTermsAndConditions").isEqualTo(true)
                .jsonPath("$.id").isEqualTo(userId.toString());
    }

    @Test
    @Order(4)
    void should_get_current_user_with_custom_avatar() throws JsonProcessingException {
        // Given
        Long githubUserId = 595505L;
        String login = "ofux";
        UUID userId = UUID.fromString("e461c019-ba23-4671-9b6c-3a5a18748af9");

        final String jwt = HasuraJwtHelper.generateValidJwtFor(jwtSecret, HasuraJwtPayload.builder()
                .iss(jwtSecret.getIssuer())
                .claims(HasuraJwtPayload.HasuraClaims.builder()
                        .userId(userId)
                        .allowedRoles(List.of("me"))
                        .githubUserId(githubUserId)
                        .avatarUrl("https://avatars.githubusercontent.com/u/595505?v=4")
                        .login(login)
                        .build())
                .build());

        // When
        client.get()
                .uri(getApiURI(ME_GET))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
                .exchange()
                // Then
                .expectStatus().is2xxSuccessful()
                .expectBody()
                .jsonPath("$.login").isEqualTo(login)
                .jsonPath("$.githubUserId").isEqualTo(githubUserId)
                .jsonPath("$.avatarUrl").isEqualTo(
                        "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/5494259449694867225.webp")
                .jsonPath("$.hasSeenOnboardingWizard").isEqualTo(true)
                .jsonPath("$.hasAcceptedLatestTermsAndConditions").isEqualTo(true)
                .jsonPath("$.id").isEqualTo(userId.toString());

        final var iamUser = userRepository.findById(userId);
        assertThat(iamUser).isPresent();
        assertThat(iamUser.get().getId()).isEqualTo(userId);
        assertThat(iamUser.get().getGithubUserId()).isEqualTo(githubUserId);
        assertThat(iamUser.get().getGithubLogin()).isEqualTo(login);
        assertThat(iamUser.get().getGithubAvatarUrl()).isEqualTo("https://avatars.githubusercontent.com/u/595505?v=4");
        assertThat(iamUser.get().getRoles()).containsExactly(UserRole.USER, UserRole.ADMIN);
        assertThat(iamUser.get().getLastSeenAt().toInstant()).isGreaterThan(LocalDateTime.parse("2023-12-14T08:00:00" +
                                                                                                ".000").toInstant(java.time.ZoneOffset.UTC));
    }
}
