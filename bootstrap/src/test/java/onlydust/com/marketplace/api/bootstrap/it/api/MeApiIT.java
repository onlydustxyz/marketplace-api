package onlydust.com.marketplace.api.bootstrap.it.api;

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import lombok.SneakyThrows;
import onlydust.com.marketplace.accounting.domain.model.*;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.BillingProfile;
import onlydust.com.marketplace.accounting.domain.model.user.UserId;
import onlydust.com.marketplace.accounting.domain.service.AccountingService;
import onlydust.com.marketplace.accounting.domain.service.BillingProfileService;
import onlydust.com.marketplace.api.bootstrap.helper.Auth0ApiClientStub;
import onlydust.com.marketplace.api.bootstrap.helper.CurrencyHelper;
import onlydust.com.marketplace.api.bootstrap.helper.UserAuthHelper;
import onlydust.com.marketplace.api.contract.model.RewardItemRequest;
import onlydust.com.marketplace.api.contract.model.RewardRequest;
import onlydust.com.marketplace.api.contract.model.RewardType;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.VerificationStatusEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.ApplicationEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.ProjectLeadEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.ProjectLeaderInvitationEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.ProjectRepoEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.ApplicationRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.ProjectLeadRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.ProjectLeaderInvitationRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.ProjectRepoRepository;
import onlydust.com.marketplace.api.rest.api.adapter.mapper.DateMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static java.lang.String.format;
import static java.util.Objects.isNull;
import static onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticationFilter.BEARER_PREFIX;
import static org.assertj.core.api.Assertions.assertThat;


public class MeApiIT extends AbstractMarketplaceApiIT {
    @Autowired
    ProjectLeaderInvitationRepository projectLeaderInvitationRepository;
    @Autowired
    ApplicationRepository applicationRepository;
    @Autowired
    ProjectLeadRepository projectLeadRepository;
    @Autowired
    ProjectRepoRepository projectRepoRepository;
    @Autowired
    BillingProfileService billingProfileService;
    @Autowired
    AccountingService accountingService;

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

    @Test
    void should_return_billing_profiles_and_missingPayoutPreference() {
        // Given
        final var authenticatedUser = userAuthHelper.newFakeUser(UUID.randomUUID(),
                faker.number().randomNumber(11, true), "another-fake-user",
                faker.internet().url(), false);

        // When user has no BP and no reward
        client.get()
                .uri(ME_GET)
                .header("Authorization", "Bearer " + authenticatedUser.jwt())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                          "billingProfiles": [],
                          "missingPayoutPreference": false
                        }
                        """);

        // When user has a not-verified BP and no reward
        final var individualBillingProfile = billingProfileService.createIndividualBillingProfile(UserId.of(authenticatedUser.user().getId()),
                faker.rickAndMorty().character(), null);
        client.get()
                .uri(ME_GET)
                .header("Authorization", "Bearer " + authenticatedUser.jwt())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                          "billingProfiles": [{
                                                "id": "%s",
                                                "type": "INDIVIDUAL",
                                                "role": "ADMIN",
                                                "verificationStatus": "NOT_STARTED",
                                                "missingPayoutInfo": false,
                                                "missingVerification": false,
                                                "verificationBlocked": false
                                              }],
                          "missingPayoutPreference": false
                        }
                        """.formatted(individualBillingProfile.id().value()));

        // When user has a not-verified BP and some reward
        final var projectId = ProjectId.of("f39b827f-df73-498c-8853-99bc3f562723");
        final var sponsorId = UUID.fromString("eb04a5de-4802-4071-be7b-9007b563d48d");
        final var usdc = currencyRepository.findByCode("USDC").orElseThrow().id();
        final var usdcSponsorAccount = accountingService.createSponsorAccountWithInitialBalance(SponsorId.of(sponsorId),
                Currency.Id.of(usdc), null,
                new SponsorAccount.Transaction(ZonedDateTime.now(), SponsorAccount.Transaction.Type.DEPOSIT, Network.ETHEREUM, faker.random().hex(),
                        PositiveAmount.of(200000L),
                        faker.rickAndMorty().character(), faker.hacker().verb()));
        accountingService.allocate(usdcSponsorAccount.account().id(), projectId, PositiveAmount.of(100000L), Currency.Id.of(usdc));
        sendRewardToRecipient(authenticatedUser.user().getGithubUserId(), 100L, projectId.value());
        client.get()
                .uri(ME_GET)
                .header("Authorization", "Bearer " + authenticatedUser.jwt())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                          "billingProfiles": [{
                                                "id": "%s",
                                                "type": "INDIVIDUAL",
                                                "role": "ADMIN",
                                                "verificationStatus": "NOT_STARTED",
                                                "missingPayoutInfo": false,
                                                "missingVerification": false,
                                                "verificationBlocked": false
                                              }],
                          "missingPayoutPreference": true
                        }
                        """.formatted(individualBillingProfile.id().value()));

        // When the user set missing payout preferences
        updatePayoutPreferences(authenticatedUser.user().getGithubUserId(), individualBillingProfile.id(), projectId.value());
        client.get()
                .uri(ME_GET)
                .header("Authorization", "Bearer " + authenticatedUser.jwt())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                          "billingProfiles": [{
                                                "id": "%s",
                                                "type": "INDIVIDUAL",
                                                "role": "ADMIN",
                                                "verificationStatus": "NOT_STARTED",
                                                "missingPayoutInfo": false,
                                                "missingVerification": true,
                                                "verificationBlocked": false
                                              }],
                          "missingPayoutPreference": false
                        }
                        """.formatted(individualBillingProfile.id().value()));

        // When the user gets his BP verification blocked
        accountingHelper.patchBillingProfile(individualBillingProfile.id().value(), null, VerificationStatusEntity.CLOSED);
        client.get()
                .uri(ME_GET)
                .header("Authorization", "Bearer " + authenticatedUser.jwt())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                          "billingProfiles": [{
                                                "id": "%s",
                                                "type": "INDIVIDUAL",
                                                "role": "ADMIN",
                                                "verificationStatus": "CLOSED",
                                                "missingPayoutInfo": false,
                                                "missingVerification": true,
                                                "verificationBlocked": true
                                              }],
                          "missingPayoutPreference": false
                        }
                        """.formatted(individualBillingProfile.id().value()));

        // When the user gets his BP verified
        accountingHelper.patchBillingProfile(individualBillingProfile.id().value(), null, VerificationStatusEntity.VERIFIED);
        client.get()
                .uri(ME_GET)
                .header("Authorization", "Bearer " + authenticatedUser.jwt())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                          "billingProfiles": [{
                                                "id": "%s",
                                                "type": "INDIVIDUAL",
                                                "role": "ADMIN",
                                                "verificationStatus": "VERIFIED",
                                                "missingPayoutInfo": true,
                                                "missingVerification": false,
                                                "verificationBlocked": false
                                              }],
                          "missingPayoutPreference": false
                        }
                        """.formatted(individualBillingProfile.id().value()));

        // When the user adds some payout infos
        client.put()
                .uri(getApiURI(BILLING_PROFILES_PUT_PAYOUT_INFO.formatted(individualBillingProfile.id().value())))
                .header("Authorization", "Bearer " + authenticatedUser.jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "aptosAddress": "0xa645c3bdd0dfd0c3628803075b3b133e8426061dc915ef996cc5ed4cece6d4e5",
                          "ethWallet": "vitalik.eth",
                          "optimismAddress": "0x72C30FCD1e7bd691Ce206Cd36BbD87C4C7099545",
                          "bankAccount": {
                            "bic": "DAAEFRPPCCT",
                            "number": "FR5417569000301995586997O41"
                          },
                          "starknetAddress": "0x056471aa79e3daebb62185cebee14fb0088b462b04ccf6e60ec9386044bec798"
                        }
                        """)
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful();
        client.get()
                .uri(ME_GET)
                .header("Authorization", "Bearer " + authenticatedUser.jwt())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                          "billingProfiles": [{
                                                "id": "%s",
                                                "type": "INDIVIDUAL",
                                                "role": "ADMIN",
                                                "verificationStatus": "VERIFIED",
                                                "missingPayoutInfo": false,
                                                "missingVerification": false,
                                                "verificationBlocked": false
                                              }],
                          "missingPayoutPreference": false
                        }
                        """.formatted(individualBillingProfile.id().value()));
    }

    @Test
    void should_return_sponsors() {
        // Given
        final var authenticatedUser = userAuthHelper.newFakeUser(UUID.randomUUID(),
                faker.number().randomNumber(11, true), "another-fake-user",
                faker.internet().url(), false);

        // When user has no sponsor
        client.get()
                .uri(ME_GET)
                .header("Authorization", "Bearer " + authenticatedUser.jwt())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                          "sponsors": []
                        }
                        """);

        addSponsorFor(authenticatedUser, SponsorId.of("58a0a05c-c81e-447c-910f-629817a987b8"));
        addSponsorFor(authenticatedUser, SponsorId.of("85435c9b-da7f-4670-bf65-02b84c5da7f0"));
        addSponsorFor(authenticatedUser, SponsorId.of("4202fd03-f316-458f-a642-421c7b3c7026"));

        // When user no sponsors
        client.get()
                .uri(ME_GET)
                .header("Authorization", "Bearer " + authenticatedUser.jwt())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                          "sponsors": [
                            {
                              "id": "58a0a05c-c81e-447c-910f-629817a987b8",
                              "name": "Captain America",
                              "url": "https://www.marvel.com/characters/captain-america-steve-rogers",
                              "logoUrl": "https://www.ed92.org/wp-content/uploads/2021/06/captain-america-2-scaled.jpg"
                            },
                            {
                              "id": "85435c9b-da7f-4670-bf65-02b84c5da7f0",
                              "name": "AS Nancy Lorraine",
                              "url": null,
                              "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/951523516066154017.png"
                            },
                            {
                              "id": "4202fd03-f316-458f-a642-421c7b3c7026",
                              "name": "ChatGPT",
                              "url": "https://chat.openai.com/",
                              "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/4216570625498269873.png"
                            }
                          ]
                        }
                        """);
    }

    private void sendRewardToRecipient(Long recipientId, Long amount, UUID projectId) {
        final UserAuthHelper.AuthenticatedUser pierre = userAuthHelper.authenticatePierre();
        final RewardRequest rewardRequest = new RewardRequest()
                .amount(BigDecimal.valueOf(amount))
                .currencyId(CurrencyHelper.USDC.value())
                .recipientId(recipientId)
                .items(List.of(
                        new RewardItemRequest().id("0011051356")
                                .type(RewardType.PULL_REQUEST)
                                .number(1L)
                                .repoId(55223344L)
                ));

        // When
        indexerApiWireMockServer.stubFor(WireMock.put(
                        WireMock.urlEqualTo("/api/v1/users/%s".formatted(recipientId)))
                .withHeader("Content-Type", equalTo("application/json"))
                .withHeader("Api-Key", equalTo("some-indexer-api-key"))
                .willReturn(ResponseDefinitionBuilder.okForEmptyJson()));

        client.post()
                .uri(getApiURI(String.format(PROJECTS_REWARDS, projectId)))
                .header("Authorization", BEARER_PREFIX + pierre.jwt())
                .body(BodyInserters.fromValue(rewardRequest))
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful();
    }

    private void updatePayoutPreferences(final Long githubUserId, BillingProfile.Id billingProfileId, final UUID projectId) {
        final UserAuthHelper.AuthenticatedUser authenticatedUser = userAuthHelper.authenticateUser(githubUserId);
        client.put()
                .uri(getApiURI(ME_PUT_PAYOUT_PREFERENCES))
                .header("Authorization", "Bearer " + authenticatedUser.jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "billingProfileId": "%s",
                          "projectId": "%s"
                        }
                        """.formatted(isNull(billingProfileId) ? null : billingProfileId.value(), projectId))
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful();
    }
}
