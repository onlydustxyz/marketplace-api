package onlydust.com.marketplace.api.bootstrap.it.api;

import lombok.SneakyThrows;
import onlydust.com.marketplace.api.bootstrap.helper.Auth0ApiClientStub;
import onlydust.com.marketplace.api.bootstrap.helper.UserAuthHelper;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.*;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.*;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.type.CurrencyEnumEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.CompanyBillingProfileRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.IndividualBillingProfileRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.UserBillingProfileTypeRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.*;
import onlydust.com.marketplace.api.rest.api.adapter.mapper.DateMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static java.lang.String.format;
import static onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticationFilter.BEARER_PREFIX;
import static org.assertj.core.api.Assertions.assertThat;


public class MeApiIT extends AbstractMarketplaceApiIT {

    @Autowired
    UserPayoutInfoRepository userPayoutInfoRepository;
    @Autowired
    ProjectLeaderInvitationRepository projectLeaderInvitationRepository;
    @Autowired
    ApplicationRepository applicationRepository;

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
        final String jwt = userAuthHelper.newFakeUser(userId, githubUserId, login, avatarUrl, false).jwt();

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

    @SneakyThrows
    @Test
    void should_accept_valid_project_leader_invitation() {
        // Given
        final var githubUserId = faker.number().randomNumber();
        final var login = faker.name().username();
        final var avatarUrl = faker.internet().avatar();
        final var userId = UUID.randomUUID();
        final String jwt = userAuthHelper.newFakeUser(userId, githubUserId, login, avatarUrl, false).jwt();

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

        notificationOutboxJob.run();
        webhookWireMockServer.verify(1, postRequestedFor(urlEqualTo("/"))
                .withHeader("Content-Type", equalTo("application/json"))
                .withRequestBody(matchingJsonPath("$.aggregate_name", equalTo("Project")))
                .withRequestBody(matchingJsonPath("$.event_name", equalTo("LeaderAssigned")))
                .withRequestBody(matchingJsonPath("$.environment", equalTo("local-it")))
                .withRequestBody(matchingJsonPath("$.payload.id", equalTo(projectId)))
                .withRequestBody(matchingJsonPath("$.payload.leader_id", equalTo(userId.toString()))));
    }

    @Test
    void should_not_accept_project_leader_invitation_when_invitation_does_not_exist() {
        // Given
        final var githubUserId = faker.number().randomNumber();
        final var login = faker.name().username();
        final var avatarUrl = faker.internet().avatar();
        final var userId = UUID.randomUUID();
        final String jwt = userAuthHelper.newFakeUser(userId, githubUserId, login, avatarUrl, false).jwt();

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

    @SneakyThrows
    @Test
    void should_apply_to_project() {
        // Given
        final var githubUserId = faker.number().numberBetween(10000, 200000);
        final var login = faker.name().username() + faker.cat().name();
        final var avatarUrl = faker.internet().avatar();
        final var userId = UUID.randomUUID();
        final String jwt = userAuthHelper.newFakeUser(userId, githubUserId, login, avatarUrl, false).jwt();

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

        notificationOutboxJob.run();
        webhookWireMockServer.verify(1, postRequestedFor(urlEqualTo("/"))
                .withHeader("Content-Type", equalTo("application/json"))
                .withRequestBody(matchingJsonPath("$.aggregate_name", equalTo("Application")))
                .withRequestBody(matchingJsonPath("$.event_name", equalTo("Received")))
                .withRequestBody(matchingJsonPath("$.environment", equalTo("local-it")))
                .withRequestBody(matchingJsonPath("$.payload.project_id", equalTo(projectId)))
                .withRequestBody(matchingJsonPath("$.payload.applicant_id", equalTo(userId.toString()))));
    }

    @Test
    void should_not_be_able_to_apply_twice() {
        // Given
        final var githubUserId = faker.number().numberBetween(100000, 2000000);
        final var login = faker.name().username() + faker.code().asin();
        final var avatarUrl = faker.internet().avatar();
        final var userId = UUID.randomUUID();
        final String jwt = userAuthHelper.newFakeUser(userId, githubUserId, login, avatarUrl, false).jwt();

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
        final String jwt = userAuthHelper.newFakeUser(userId, githubUserId, login, avatarUrl, false).jwt();

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
    void should_return_projects_led_and_applications() {
        // Given
        final UserAuthHelper.AuthenticatedUser pierre = userAuthHelper.authenticatePierre();
        final String jwt = pierre.jwt();
        projectLeadRepository.save(new ProjectLeadEntity(UUID.fromString("7d04163c-4187-4313-8066-61504d34fc56"),
                pierre.user().getId()));
        final UUID projectIdWithoutRepo = UUID.fromString(
                "c6940f66-d64e-4b29-9a7f-07abf5c3e0ed");
        projectLeaderInvitationRepository.save(new ProjectLeaderInvitationEntity(UUID.randomUUID(),
                projectIdWithoutRepo, pierre.user().getGithubUserId()));

        final var projectAppliedTo1 = UUID.randomUUID();
        final var projectAppliedTo2 = UUID.randomUUID();
        applicationRepository.save(ApplicationEntity.builder()
                .id(UUID.randomUUID())
                .projectId(projectAppliedTo1)
                .applicantId(pierre.user().getId())
                .receivedAt(new Date())
                .build());
        applicationRepository.save(ApplicationEntity.builder()
                .id(UUID.randomUUID())
                .projectId(projectAppliedTo2)
                .applicantId(pierre.user().getId())
                .receivedAt(new Date())
                .build());

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
                .jsonPath("$.createdAt").isEqualTo(DateMapper.toZoneDateTime(pierre.user().getCreatedAt()).format(DateTimeFormatter.ISO_INSTANT))
                .jsonPath("$.projectsLed[0].slug").isEqualTo("bretzel")
                .jsonPath("$.pendingProjectsLed.length()").isEqualTo(0)
                .jsonPath("$.projectsAppliedTo.length()").isEqualTo(2)
                .jsonPath("$.projectsAppliedTo[0]").isEqualTo(projectAppliedTo1.toString())
                .jsonPath("$.projectsAppliedTo[1]").isEqualTo(projectAppliedTo2.toString());

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
                .jsonPath("$.pendingProjectsLed.length()").isEqualTo(1)
                .jsonPath("$.projectsAppliedTo.length()").isEqualTo(2)
                .jsonPath("$.projectsAppliedTo[0]").isEqualTo(projectAppliedTo1.toString())
                .jsonPath("$.projectsAppliedTo[1]").isEqualTo(projectAppliedTo2.toString());
    }

    @Test
    void should_update_last_seen_at() {
        // Given
        final UserAuthHelper.AuthenticatedUser pierre = userAuthHelper.authenticatePierre();
        final var before = new Date();

        // When
        client.get()
                .uri(ME_GET)
                .header("Authorization", BEARER_PREFIX + pierre.jwt())
                .exchange()
                .expectStatus()
                .is2xxSuccessful();

        final var after = new Date();

        // Then
        final var user = userRepository.findByGithubUserId(pierre.user().getGithubUserId()).orElseThrow();
        assertThat(user.getLastSeenAt()).isAfter(before);
        assertThat(user.getLastSeenAt()).isBefore(after);
    }

    @Test
    void should_contains_first_and_last_names() {
        // Given
        final UserAuthHelper.AuthenticatedUser pierre = userAuthHelper.authenticatePierre();

        // When
        client.get()
                .uri(ME_GET)
                .header("Authorization", BEARER_PREFIX + pierre.jwt())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.firstName").isEqualTo("Pierre")
                .jsonPath("$.lastName").isEqualTo("Oucif");

    }

    @Autowired
    Auth0ApiClientStub auth0ApiClientStub;

    @Test
    void should_update_github_profile_data() {
        // Given
        final UserAuthHelper.AuthenticatedUser authenticatedUser = userAuthHelper.newFakeUser(UUID.randomUUID(),
                faker.number().numberBetween(1, 100) + faker.number().numberBetween(200, 1000), "fake-user",
                faker.internet().url(), false);
        final String githubPat = faker.rickAndMorty().character();
        auth0ApiClientStub.withPat(authenticatedUser.user().getGithubUserId(), githubPat);
        final String newUrl = faker.internet().url() + "/test";
        final String newLogin = faker.gameOfThrones().character();
        final String newEmail = faker.internet().emailAddress();

        // When
        githubWireMockServer.stubFor(get("/user").withHeader("Authorization", equalTo("Bearer " + githubPat))
                .willReturn(okJson("""
                        {
                            "id": %d,
                            "login": \"%s\",
                            "avatar_url": \"%s\"
                        }
                        """.formatted(authenticatedUser.user().getGithubUserId(), newLogin, newUrl)))
        );
        githubWireMockServer.stubFor(get("/user/emails").withHeader("Authorization", equalTo("Bearer " + githubPat))
                .willReturn(okJson("""
                        [
                            {
                                "email": \"%s\",
                                "primary": true
                            }
                        ]
                        """.formatted(newEmail)))
        );


        client.get()
                .uri(ME_GET_PROFILE_GITHUB)
                .header("Authorization", BEARER_PREFIX + authenticatedUser.jwt())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful();

        client.get()
                .uri(ME_GET)
                .header("Authorization", BEARER_PREFIX + authenticatedUser.jwt())
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.login").isEqualTo(newLogin)
                .jsonPath("$.avatarUrl").isEqualTo(newUrl)
                .jsonPath("$.email").isEqualTo(newEmail);
    }

    @Autowired
    PaymentRequestRepository paymentRequestRepository;
    @Autowired
    UserBillingProfileTypeRepository userBillingProfileTypeRepository;
    @Autowired
    CompanyBillingProfileRepository companyBillingProfileRepository;
    @Autowired
    IndividualBillingProfileRepository individualBillingProfileRepository;

    @Test
    void should_return_has_valid_billing_profile() {
        // Given
        final UserAuthHelper.AuthenticatedUser authenticatedUser = userAuthHelper.newFakeUser(UUID.randomUUID(),
                faker.number().randomNumber() + faker.number().randomNumber(), faker.hacker().abbreviation(),
                faker.internet().url(), false);

        // When
        client.get()
                .uri(ME_GET)
                .header("Authorization", "Bearer " + authenticatedUser.jwt())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.hasValidBillingProfile").isEqualTo(true);

        final UserEntity user = authenticatedUser.user();
        paymentRequestRepository.save(new PaymentRequestEntity(UUID.randomUUID(), user.getId(), user.getGithubUserId(), new Date(), BigDecimal.ONE, null,
                0, UUID.randomUUID(), CurrencyEnumEntity.usdc));

        // When
        client.get()
                .uri(ME_GET)
                .header("Authorization", "Bearer " + authenticatedUser.jwt())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.hasValidBillingProfile").isEqualTo(false);

        userBillingProfileTypeRepository.save(UserBillingProfileTypeEntity.builder()
                .userId(user.getId())
                .billingProfileType(UserBillingProfileTypeEntity.BillingProfileTypeEntity.COMPANY)
                .build());
        companyBillingProfileRepository.save(CompanyBillingProfileEntity.builder()
                .verificationStatus(OldVerificationStatusEntity.VERIFIED)
                .id(UUID.randomUUID())
                .userId(user.getId())
                .build());

        // When
        client.get()
                .uri(ME_GET)
                .header("Authorization", "Bearer " + authenticatedUser.jwt())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.hasValidBillingProfile").isEqualTo(true);

        userBillingProfileTypeRepository.save(UserBillingProfileTypeEntity.builder()
                .userId(user.getId())
                .billingProfileType(UserBillingProfileTypeEntity.BillingProfileTypeEntity.INDIVIDUAL)
                .build());

        // When
        client.get()
                .uri(ME_GET)
                .header("Authorization", "Bearer " + authenticatedUser.jwt())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.hasValidBillingProfile").isEqualTo(false);

        individualBillingProfileRepository.save(IndividualBillingProfileEntity.builder()
                .verificationStatus(OldVerificationStatusEntity.VERIFIED)
                .userId(user.getId())
                .id(UUID.randomUUID())
                .build());

        // When
        client.get()
                .uri(ME_GET)
                .header("Authorization", "Bearer " + authenticatedUser.jwt())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.hasValidBillingProfile").isEqualTo(true);
    }
}
