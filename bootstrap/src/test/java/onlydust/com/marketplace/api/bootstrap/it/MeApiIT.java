package onlydust.com.marketplace.api.bootstrap.it;

import onlydust.com.marketplace.api.bootstrap.helper.HasuraUserHelper;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.ApplicationEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.ProjectLeaderInvitationEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.ApplicationRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.ProjectLeaderInvitationRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.UserPayoutInfoRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;

import java.util.Date;
import java.util.UUID;

import static java.lang.String.format;
import static onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticationFilter.BEARER_PREFIX;

@ActiveProfiles({"hasura_auth"})
public class MeApiIT extends AbstractMarketplaceApiIT {

    @Autowired
    UserPayoutInfoRepository userPayoutInfoRepository;
    @Autowired
    ProjectLeaderInvitationRepository projectLeaderInvitationRepository;
    @Autowired
    ApplicationRepository applicationRepository;
    @Autowired
    HasuraUserHelper userHelper;


    @Test
    void should_update_onboarding_state() {
        // Given
        final var githubUserId = faker.number().randomNumber();
        final var login = faker.name().username();
        final var avatarUrl = faker.internet().avatar();
        final var userId = UUID.randomUUID();
        final String jwt = userHelper.newFakeUser(userId, githubUserId, login, avatarUrl, false).jwt();

        client.get()
                .uri(getApiURI(ME_GET))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody()
                .jsonPath("$.hasSeenOnboardingWizard").isEqualTo(false)
                .jsonPath("$.hasAcceptedLatestTermsAndConditions").isEqualTo(false)
                .jsonPath("$.hasValidPayoutInfos").isEqualTo(true);

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
                .jsonPath("$.hasAcceptedLatestTermsAndConditions").isEqualTo(false)
                .jsonPath("$.hasValidPayoutInfos").isEqualTo(true);

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
                .jsonPath("$.hasAcceptedLatestTermsAndConditions").isEqualTo(true)
                .jsonPath("$.hasValidPayoutInfos").isEqualTo(true);
    }


    @Test
    void should_accept_valid_project_leader_invitation() {
        // Given
        final var githubUserId = faker.number().randomNumber();
        final var login = faker.name().username();
        final var avatarUrl = faker.internet().avatar();
        final var userId = UUID.randomUUID();
        final String jwt = userHelper.newFakeUser(userId, githubUserId, login, avatarUrl, false).jwt();

        final String projectId = "7d04163c-4187-4313-8066-61504d34fc56";

        projectLeaderInvitationRepository.save(new ProjectLeaderInvitationEntity(UUID.randomUUID(),
                UUID.fromString(projectId), githubUserId));

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
    void should_not_accept_project_leader_invitation_when_invitation_does_not_exist() {
        // Given
        final var githubUserId = faker.number().randomNumber();
        final var login = faker.name().username();
        final var avatarUrl = faker.internet().avatar();
        final var userId = UUID.randomUUID();
        final String jwt = userHelper.newFakeUser(userId, githubUserId, login, avatarUrl, false).jwt();

        final String projectId = "7d04163c-4187-4313-8066-61504d34fc56";

        projectLeaderInvitationRepository.save(new ProjectLeaderInvitationEntity(UUID.randomUUID(),
                UUID.fromString(projectId), faker.number().randomNumber()));

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

    @Test
    void should_apply_to_project() {
        // Given
        final var githubUserId = faker.number().numberBetween(10000, 200000);
        final var login = faker.name().username() + faker.cat().name();
        final var avatarUrl = faker.internet().avatar();
        final var userId = UUID.randomUUID();
        final String jwt = userHelper.newFakeUser(userId, githubUserId, login, avatarUrl, false).jwt();

        final String projectId = "7d04163c-4187-4313-8066-61504d34fc56";

        // When
        client.post()
                .uri(getApiURI(ME_APPLY_TO_PROJECT))
                .header("Authorization", BEARER_PREFIX + jwt)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                            "projectId": "%s"
                        }
                        """.formatted(projectId))
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful();
    }

    @Test
    void should_not_be_able_to_apply_twice() {
        // Given
        final var githubUserId = faker.number().numberBetween(100000, 2000000);
        final var login = faker.name().username() + faker.code().asin();
        final var avatarUrl = faker.internet().avatar();
        final var userId = UUID.randomUUID();
        final String jwt = userHelper.newFakeUser(userId, githubUserId, login, avatarUrl, false).jwt();

        final String projectId = "7d04163c-4187-4313-8066-61504d34fc56";

        applicationRepository.save(ApplicationEntity.builder()
                .id(UUID.randomUUID())
                .projectId(UUID.fromString(projectId))
                .applicantId(userId)
                .receivedAt(new Date())
                .build());

        // When
        client.post()
                .uri(getApiURI(ME_APPLY_TO_PROJECT))
                .header("Authorization", BEARER_PREFIX + jwt)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                            "projectId": "%s"
                        }
                        """.formatted(projectId))
                // Then
                .exchange()
                .expectStatus()
                .is4xxClientError();
    }

    @Test
    void should_not_be_able_to_apply_to_non_existing_project() {
        // Given
        final var githubUserId = faker.number().randomNumber();
        final var login = faker.name().username();
        final var avatarUrl = faker.internet().avatar();
        final var userId = UUID.randomUUID();
        final String jwt = userHelper.newFakeUser(userId, githubUserId, login, avatarUrl, false).jwt();

        final String projectId = "77777777-4444-4444-4444-61504d34fc56";

        // When
        client.post()
                .uri(getApiURI(ME_APPLY_TO_PROJECT))
                .header("Authorization", BEARER_PREFIX + jwt)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                            "projectId": "%s"
                        }
                        """.formatted(projectId))
                // Then
                .exchange()
                .expectStatus()
                .isNotFound();
    }

    @Test
    void should_return_project_led_ids_and_slugs() {
        // Given
        final String jwt = userHelper.authenticatePierre().jwt();

        // When
        client.get()
                .uri(ME_GET)
                .header("Authorization", BEARER_PREFIX + jwt)
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.projectLedIds[0].id").isEqualTo("f39b827f-df73-498c-8853-99bc3f562723")
                .jsonPath("$.projectLedIds[0].slug").isEqualTo("qa-new-contributions");
    }
}
