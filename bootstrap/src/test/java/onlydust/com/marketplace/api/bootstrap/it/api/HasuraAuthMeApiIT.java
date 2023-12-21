package onlydust.com.marketplace.api.bootstrap.it.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.SneakyThrows;
import onlydust.com.marketplace.api.bootstrap.helper.HasuraJwtHelper;
import onlydust.com.marketplace.api.domain.model.UserRole;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.UserEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.AuthUserEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.OnboardingEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.UserRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.AuthUserRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.OnboardingRepository;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticationFilter;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.hasura.HasuraJwtPayload;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.jwt.JwtSecret;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;


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
        Long githubUserId = faker.number().randomNumber(15, true);
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
        assertThat(iamUser.get().getGithubEmail()).isEqualTo(email);
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
                .jsonPath("$.isAdmin").isEqualTo(false)
                .jsonPath("$.id").isEqualTo(userId.toString());
    }

    @Test
    @Order(3)
    void should_get_current_user_with_onboarding_data() throws JsonProcessingException {
        // Given
        Long githubUserId = faker.number().randomNumber(15, true);
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
        assertThat(iamUser.get().getGithubEmail()).isEqualTo(email);
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
                .jsonPath("$.isAdmin").isEqualTo(false)
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
                .jsonPath("$.isAdmin").isEqualTo(true)
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

    @Test
    @Order(5)
    void should_fail_to_get_unexisting_user() throws JsonProcessingException {
        // Given
        Long githubUserId = faker.number().randomNumber(15, true);
        String login = faker.name().username();
        String avatarUrl = faker.internet().avatar();
        UUID userId = UUID.randomUUID();

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
                .expectStatus().is4xxClientError();
    }

    @SneakyThrows
    @Test
    void should_get_impersonated_user() {
        // Given
        final UserEntity impersonatorUser = UserEntity.builder()
                .id(UUID.randomUUID())
                .githubUserId(faker.number().randomNumber(15, true))
                .githubLogin(faker.name().username())
                .githubAvatarUrl(faker.internet().avatar())
                .githubEmail(faker.internet().emailAddress())
                .lastSeenAt(new Date())
                .roles(new UserRole[]{UserRole.USER, UserRole.ADMIN})
                .build();
        userRepository.save(impersonatorUser);

        final UserEntity impersonatedUser = UserEntity.builder()
                .id(UUID.randomUUID())
                .githubUserId(impersonatorUser.getGithubUserId() + faker.number().numberBetween(1, 1000))
                .githubLogin(faker.name().username())
                .githubAvatarUrl(faker.internet().avatar())
                .githubEmail(faker.internet().emailAddress())
                .lastSeenAt(new Date())
                .roles(new UserRole[]{UserRole.USER})
                .build();
        userRepository.save(impersonatedUser);

        final String jwt = HasuraJwtHelper.generateValidJwtFor(jwtSecret, HasuraJwtPayload.builder()
                .iss(jwtSecret.getIssuer())
                .claims(HasuraJwtPayload.HasuraClaims.builder()
                        .userId(impersonatorUser.getId())
                        .allowedRoles(List.of("me"))
                        .githubUserId(impersonatorUser.getGithubUserId())
                        .avatarUrl(impersonatorUser.getGithubAvatarUrl())
                        .login(impersonatorUser.getGithubLogin())
                        .build())
                .build());

        // When
        client.get()
                .uri(getApiURI(ME_GET))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
                .header(AuthenticationFilter.IMPERSONATION_HEADER,
                        "{\"x-hasura-githubUserId\":%d}".formatted(impersonatedUser.getGithubUserId())
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

    @SneakyThrows
    @Test
    void should_fail_to_impersonate_non_registered_user() {
        // Given
        final UserEntity impersonatorUser = UserEntity.builder()
                .id(UUID.randomUUID())
                .githubUserId(faker.number().randomNumber(15, true))
                .githubLogin(faker.name().username())
                .githubAvatarUrl(faker.internet().avatar())
                .githubEmail(faker.internet().emailAddress())
                .lastSeenAt(new Date())
                .roles(new UserRole[]{UserRole.USER, UserRole.ADMIN})
                .build();
        userRepository.save(impersonatorUser);

        final long impersonatedUserId = impersonatorUser.getGithubUserId() + faker.number().numberBetween(1, 1000);

        final String jwt = HasuraJwtHelper.generateValidJwtFor(jwtSecret, HasuraJwtPayload.builder()
                .iss(jwtSecret.getIssuer())
                .claims(HasuraJwtPayload.HasuraClaims.builder()
                        .userId(impersonatorUser.getId())
                        .allowedRoles(List.of("me"))
                        .githubUserId(impersonatorUser.getGithubUserId())
                        .avatarUrl(impersonatorUser.getGithubAvatarUrl())
                        .login(impersonatorUser.getGithubLogin())
                        .build())
                .build());

        // When
        client.get()
                .uri(getApiURI(ME_GET))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
                .header(AuthenticationFilter.IMPERSONATION_HEADER,
                        "{\"x-hasura-githubUserId\":%d}".formatted(impersonatedUserId)
                )
                .exchange()
                // Then
                .expectStatus().isUnauthorized();
    }

    @SneakyThrows
    @Test
    void should_fail_to_impersonate_user_when_not_admin() {
        // Given
        final UserEntity impersonatorUser = UserEntity.builder()
                .id(UUID.randomUUID())
                .githubUserId(faker.number().randomNumber(15, true))
                .githubLogin(faker.name().username())
                .githubAvatarUrl(faker.internet().avatar())
                .githubEmail(faker.internet().emailAddress())
                .lastSeenAt(new Date())
                .roles(new UserRole[]{UserRole.USER})
                .build();
        userRepository.save(impersonatorUser);

        final UserEntity impersonatedUser = UserEntity.builder()
                .id(UUID.randomUUID())
                .githubUserId(impersonatorUser.getGithubUserId() + faker.number().numberBetween(1, 1000))
                .githubLogin(faker.name().username())
                .githubAvatarUrl(faker.internet().avatar())
                .githubEmail(faker.internet().emailAddress())
                .lastSeenAt(new Date())
                .roles(new UserRole[]{UserRole.USER})
                .build();
        userRepository.save(impersonatedUser);

        final String jwt = HasuraJwtHelper.generateValidJwtFor(jwtSecret, HasuraJwtPayload.builder()
                .iss(jwtSecret.getIssuer())
                .claims(HasuraJwtPayload.HasuraClaims.builder()
                        .userId(impersonatorUser.getId())
                        .allowedRoles(List.of("me"))
                        .githubUserId(impersonatorUser.getGithubUserId())
                        .avatarUrl(impersonatorUser.getGithubAvatarUrl())
                        .login(impersonatorUser.getGithubLogin())
                        .build())
                .build());

        // When
        client.get()
                .uri(getApiURI(ME_GET))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
                .header(AuthenticationFilter.IMPERSONATION_HEADER,
                        "{\"x-hasura-githubUserId\":%d}".formatted(impersonatedUser.getGithubUserId())
                )
                .exchange()
                // Then
                .expectStatus().isUnauthorized();
    }
}
