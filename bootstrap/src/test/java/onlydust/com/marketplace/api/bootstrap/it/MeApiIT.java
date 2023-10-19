package onlydust.com.marketplace.api.bootstrap.it;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.NonNull;
import onlydust.com.marketplace.api.bootstrap.helper.HasuraJwtHelper;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.AuthUserEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.ProjectLeaderInvitationEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.AuthUserRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.ProjectLeaderInvitationRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.UserPayoutInfoRepository;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.hasura.HasuraJwtPayload;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.jwt.JwtSecret;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import static java.lang.String.format;
import static onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticationFilter.BEARER_PREFIX;

@ActiveProfiles({"hasura_auth"})
public class MeApiIT extends AbstractMarketplaceApiIT {

    @Autowired
    AuthUserRepository authUserRepository;
    @Autowired
    JwtSecret jwtSecret;
    @Autowired
    UserPayoutInfoRepository userPayoutInfoRepository;
    @Autowired
    ProjectLeaderInvitationRepository projectLeaderInvitationRepository;

    @Test
    void should_get_user_payout_info() throws JsonProcessingException {
        // Given
        final AuthUserEntity anthony = authUserRepository.findByGithubUserId(43467246L).orElseThrow();
        final String jwt = HasuraJwtHelper.generateValidJwtFor(jwtSecret, HasuraJwtPayload.builder()
                .iss(jwtSecret.getIssuer())
                .claims(HasuraJwtPayload.HasuraClaims.builder()
                        .userId(anthony.getId())
                        .allowedRoles(List.of("me"))
                        .githubUserId(anthony.getGithubUserId())
                        .avatarUrl(anthony.getAvatarUrlAtSignup())
                        .login(anthony.getLoginAtSignup())
                        .build())
                .build());

        // When
        client.get()
                .uri(getApiURI(ME_GET_PAYOUT_INFO))
                .header("Authorization", BEARER_PREFIX + jwt)
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.isCompany").isEqualTo(false)
                .jsonPath("$.person.lastname").isEqualTo("BUISSET")
                .jsonPath("$.person.firstname").isEqualTo("Anthony")
                .jsonPath("$.location.address").isEqualTo("771 chemin de la sine")
                .jsonPath("$.location.city").isEqualTo("Vence")
                .jsonPath("$.location.country").isEqualTo("France")
                .jsonPath("$.location.postalCode").isEqualTo("06140")
                .jsonPath("$.payoutSettings.ethName").isEqualTo("abuisset.eth")
                .jsonPath("$.payoutSettings.usdPreferredMethod").isEqualTo("USDC");
    }

    @Test
    void should_update_onboarding_state() throws JsonProcessingException {
        // Given
        final var githubUserId = faker.number().randomNumber();
        final var login = faker.name().username();
        final var avatarUrl = faker.internet().avatar();
        final var userId = UUID.randomUUID();
        final String jwt = newFakeUser(userId, githubUserId, login, avatarUrl);

        client.get()
                .uri(getApiURI(ME_GET))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody()
                .jsonPath("$.hasSeenOnboardingWizard").isEqualTo(false)
                .jsonPath("$.hasAcceptedLatestTermsAndConditions").isEqualTo(false);

        // When
        client.patch()
                .uri(ME_PATCH)
                .header("Authorization", BEARER_PREFIX + jwt)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                            "hasSeenOnboardingWizard": true
                        }
                        """)
                .exchange()
                .expectStatus()
                .is2xxSuccessful();

        // Then
        client.get()
                .uri(getApiURI(ME_GET))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody()
                .jsonPath("$.hasSeenOnboardingWizard").isEqualTo(true)
                .jsonPath("$.hasAcceptedLatestTermsAndConditions").isEqualTo(false);

        // When
        client.patch()
                .uri(ME_PATCH)
                .header("Authorization", BEARER_PREFIX + jwt)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                            "hasAcceptedTermsAndConditions": true
                        }
                        """)
                .exchange()
                .expectStatus()
                .is2xxSuccessful();

        // Then
        client.get()
                .uri(getApiURI(ME_GET))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody()
                .jsonPath("$.hasSeenOnboardingWizard").isEqualTo(true)
                .jsonPath("$.hasAcceptedLatestTermsAndConditions").isEqualTo(true);
    }

    @NonNull
    private String newFakeUser(UUID userId, long githubUserId, String login, String avatarUrl) throws JsonProcessingException {
        final AuthUserEntity user = AuthUserEntity.builder()
                .id(userId)
                .githubUserId(githubUserId)
                .loginAtSignup(login)
                .avatarUrlAtSignup(avatarUrl)
                .isAdmin(false)
                .createdAt(new Date())
                .build();
        authUserRepository.save(user);

        return HasuraJwtHelper.generateValidJwtFor(jwtSecret, HasuraJwtPayload.builder()
                .iss(jwtSecret.getIssuer())
                .claims(HasuraJwtPayload.HasuraClaims.builder()
                        .userId(userId)
                        .allowedRoles(List.of("me"))
                        .githubUserId(githubUserId)
                        .avatarUrl(avatarUrl)
                        .login(login)
                        .build())
                .build());
    }

    @Test
    void should_accept_valid_project_leader_invitation() throws JsonProcessingException {
        // Given
        final var githubUserId = faker.number().randomNumber();
        final var login = faker.name().username();
        final var avatarUrl = faker.internet().avatar();
        final var userId = UUID.randomUUID();
        final String jwt = newFakeUser(userId, githubUserId, login, avatarUrl);

        final String projectId = "7d04163c-4187-4313-8066-61504d34fc56";

        projectLeaderInvitationRepository.save(ProjectLeaderInvitationEntity.builder()
                .id(UUID.randomUUID())
                .projectId(UUID.fromString(projectId))
                .githubUserId(githubUserId)
                .build());

        // When
        client.put()
                .uri(getApiURI(format(ME_ACCEPT_PROJECT_LEADER_INVITATION, projectId)))
                .header("Authorization", BEARER_PREFIX + jwt)
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful();

        client.get()
                .uri(getApiURI(PROJECTS_GET_BY_ID + "/" + projectId))
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath(format("$.leaders[?(@.githubUserId==%d)]", githubUserId)).exists();
    }

    @Test
    void should_not_accept_project_leader_invitation_when_invitation_does_not_exist() throws JsonProcessingException {
        // Given
        final var githubUserId = faker.number().randomNumber();
        final var login = faker.name().username();
        final var avatarUrl = faker.internet().avatar();
        final var userId = UUID.randomUUID();
        final String jwt = newFakeUser(userId, githubUserId, login, avatarUrl);

        final String projectId = "7d04163c-4187-4313-8066-61504d34fc56";

        projectLeaderInvitationRepository.save(ProjectLeaderInvitationEntity.builder()
                .id(UUID.randomUUID())
                .projectId(UUID.fromString(projectId))
                .githubUserId(faker.number().randomNumber()) // <- another user
                .build());

        // When
        client.put()
                .uri(getApiURI(format(ME_ACCEPT_PROJECT_LEADER_INVITATION, projectId)))
                .header("Authorization", BEARER_PREFIX + jwt)
                // Then
                .exchange()
                .expectStatus()
                .isNotFound();

        client.get()
                .uri(getApiURI(PROJECTS_GET_BY_ID + "/" + projectId))
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath(format("$.leaders[?(@.githubUserId==%d)]", githubUserId)).doesNotExist();
    }
}
