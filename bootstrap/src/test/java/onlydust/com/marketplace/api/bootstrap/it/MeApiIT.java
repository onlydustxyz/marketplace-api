package onlydust.com.marketplace.api.bootstrap.it;

import onlydust.com.marketplace.api.bootstrap.helper.HasuraUserHelper;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.ApplicationEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.ProjectLeadEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.ProjectLeaderInvitationEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.ProjectRepoEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.*;
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
    @Autowired
    ProjectLeadRepository projectLeadRepository;
    @Autowired
    ProjectRepoRepository projectRepoRepository;

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
    void should_return_projects_led() {
        // Given
        final HasuraUserHelper.AuthenticatedUser pierre = userHelper.authenticatePierre();
        final String jwt = pierre.jwt();
        projectLeadRepository.save(new ProjectLeadEntity(UUID.fromString("7d04163c-4187-4313-8066-61504d34fc56"),
                pierre.user().getId()));
        final UUID projectIdWithoutRepo = UUID.fromString(
                "c6940f66-d64e-4b29-9a7f-07abf5c3e0ed");
        projectLeaderInvitationRepository.save(new ProjectLeaderInvitationEntity(UUID.randomUUID(),
                projectIdWithoutRepo, pierre.user().getGithubUserId()));

        // When
        client.get()
                .uri(ME_GET)
                .header("Authorization", BEARER_PREFIX + jwt)
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.projectsLed[1].id").isEqualTo("f39b827f-df73-498c-8853-99bc3f562723")
                .jsonPath("$.projectsLed[1].name").isEqualTo("QA new contributions")
                .jsonPath("$.projectsLed[1].logoUrl").isEqualTo(null)
                .jsonPath("$.projectsLed[1].slug").isEqualTo("qa-new-contributions")
                .jsonPath("$.projectsLed[1].contributorCount").isEqualTo(18)
                .jsonPath("$.projectsLed[0].id").isEqualTo("7d04163c-4187-4313-8066-61504d34fc56")
                .jsonPath("$.projectsLed[0].name").isEqualTo("Bretzel")
                .jsonPath("$.projectsLed[0].contributorCount").isEqualTo(4)
                .jsonPath("$.projectsLed[0].logoUrl").isEqualTo("https://onlydust-app-images.s3.eu-west-1.amazonaws" +
                                                                ".com/5003677688814069549.png")
                .jsonPath("$.projectsLed[0].slug").isEqualTo("bretzel")
                .jsonPath("$.pendingProjectsLed.length()").isEqualTo(0);

        // Public repo
        projectRepoRepository.save(new ProjectRepoEntity(projectIdWithoutRepo, 593218280L));

        // When
        client.get()
                .uri(ME_GET)
                .header("Authorization", BEARER_PREFIX + jwt)
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.pendingProjectsLed[0].id").isEqualTo("c6940f66-d64e-4b29-9a7f-07abf5c3e0ed")
                .jsonPath("$.pendingProjectsLed[0].name").isEqualTo("Red bull")
                .jsonPath("$.pendingProjectsLed[0].contributorCount").isEqualTo(0)
                .jsonPath("$.pendingProjectsLed[0].logoUrl").isEqualTo("https://cdn.filestackcontent" +
                                                                       ".com/cZCHED10RzuEloOXuk7A")
                .jsonPath("$.pendingProjectsLed[0].slug").isEqualTo("red-bull")
                .jsonPath("$.pendingProjectsLed.length()").isEqualTo(1);


    }
}
