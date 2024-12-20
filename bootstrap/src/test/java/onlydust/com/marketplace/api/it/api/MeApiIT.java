package onlydust.com.marketplace.api.it.api;

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import lombok.SneakyThrows;
import onlydust.com.marketplace.accounting.domain.model.Currency;
import onlydust.com.marketplace.accounting.domain.model.Network;
import onlydust.com.marketplace.accounting.domain.model.PositiveAmount;
import onlydust.com.marketplace.accounting.domain.model.SponsorAccount;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.BillingProfile;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.VerificationStatus;
import onlydust.com.marketplace.accounting.domain.service.AccountingService;
import onlydust.com.marketplace.accounting.domain.service.BillingProfileService;
import onlydust.com.marketplace.api.contract.model.RewardItemRequest;
import onlydust.com.marketplace.api.contract.model.RewardRequest;
import onlydust.com.marketplace.api.contract.model.RewardType;
import onlydust.com.marketplace.api.helper.Auth0ApiClientStub;
import onlydust.com.marketplace.api.helper.CurrencyHelper;
import onlydust.com.marketplace.api.helper.UserAuthHelper;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.ProjectLeadEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.ProjectLeaderInvitationEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.ProjectRepoEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.ProjectLeadRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.ProjectLeaderInvitationRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.ProjectRepoRepository;
import onlydust.com.marketplace.api.rest.api.adapter.mapper.DateMapper;
import onlydust.com.marketplace.api.suites.tags.TagMe;
import onlydust.com.marketplace.kernel.model.ProjectId;
import onlydust.com.marketplace.kernel.model.SponsorId;
import onlydust.com.marketplace.kernel.model.UserId;
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

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static java.lang.String.format;
import static java.util.Objects.isNull;
import static onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticationFilter.BEARER_PREFIX;
import static org.assertj.core.api.Assertions.assertThat;

@TagMe
public class MeApiIT extends AbstractMarketplaceApiIT {
    @Autowired
    ProjectLeaderInvitationRepository projectLeaderInvitationRepository;
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
        final String jwt = userAuthHelper.signUpUser(githubUserId, login, avatarUrl, false).jwt();

        client.get()
                .uri(getApiURI(ME))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody()
                .jsonPath("$.hasCompletedOnboarding").isEqualTo(false)
                .jsonPath("$.hasAcceptedLatestTermsAndConditions").isEqualTo(false);

        // When
        client.patch()
                .uri(ME)
                .header("Authorization", BEARER_PREFIX + jwt)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                            "hasCompletedOnboarding": true
                        }
                        """)
                .exchange()
                .expectStatus()
                .is2xxSuccessful();

        // Then
        client.get()
                .uri(getApiURI(ME))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody()
                .jsonPath("$.hasCompletedOnboarding").isEqualTo(true)
                .jsonPath("$.hasAcceptedLatestTermsAndConditions").isEqualTo(false);

        // When
        client.patch()
                .uri(ME)
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
                .uri(getApiURI(ME))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody()
                .jsonPath("$.hasCompletedOnboarding").isEqualTo(true)
                .jsonPath("$.hasAcceptedLatestTermsAndConditions").isEqualTo(true);
    }

    @SneakyThrows
    @Test
    void should_accept_valid_project_leader_invitation() {
        // Given
        final var githubUserId = faker.number().randomNumber();
        final var login = faker.name().username();
        final var avatarUrl = faker.internet().avatar();
        final String jwt = userAuthHelper.signUpUser(githubUserId, login, avatarUrl, false).jwt();

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
        final String jwt = userAuthHelper.signUpUser(githubUserId, login, avatarUrl, false).jwt();

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
    void should_return_projects_led() {
        // Given
        final UserAuthHelper.AuthenticatedUser pierre = userAuthHelper.authenticatePierre();
        final String jwt = pierre.jwt();
        projectLeadRepository.save(new ProjectLeadEntity(UUID.fromString("7d04163c-4187-4313-8066-61504d34fc56"),
                pierre.user().getId()));
        final var projectIdWithoutRepo = UUID.fromString(
                "c6940f66-d64e-4b29-9a7f-07abf5c3e0ed");
        projectLeaderInvitationRepository.save(new ProjectLeaderInvitationEntity(UUID.randomUUID(),
                projectIdWithoutRepo, pierre.user().getGithubUserId()));

        // When
        client.get()
                .uri(ME)
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
                .jsonPath("$.projectsLed[0].id").isEqualTo("7d04163c-4187-4313-8066-61504d34fc56")
                .jsonPath("$.projectsLed[0].name").isEqualTo("Bretzel")
                .jsonPath("$.projectsLed[0].logoUrl").isEqualTo("https://onlydust-app-images.s3.eu-west-1.amazonaws.com/5003677688814069549.png")
                .jsonPath("$.createdAt").isEqualTo(DateMapper.toZoneDateTime(pierre.user().getCreatedAt()).format(DateTimeFormatter.ISO_INSTANT))
                .jsonPath("$.projectsLed[0].slug").isEqualTo("bretzel")
                .jsonPath("$.pendingProjectsLed.length()").isEqualTo(1);

        // Public repo
        projectRepoRepository.save(new ProjectRepoEntity(projectIdWithoutRepo, 593218280L));

        // When
        client.get()
                .uri(ME)
                .header("Authorization", BEARER_PREFIX + jwt)
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.pendingProjectsLed[0].id").isEqualTo("c6940f66-d64e-4b29-9a7f-07abf5c3e0ed")
                .jsonPath("$.pendingProjectsLed[0].name").isEqualTo("Red bull")
                .jsonPath("$.pendingProjectsLed[0].logoUrl").isEqualTo("https://cdn.filestackcontent.com/cZCHED10RzuEloOXuk7A")
                .jsonPath("$.pendingProjectsLed[0].slug").isEqualTo("red-bull")
                .jsonPath("$.pendingProjectsLed.length()").isEqualTo(1);
    }

    @Test
    void should_update_last_seen_at() {
        // Given
        final UserAuthHelper.AuthenticatedUser olivier = userAuthHelper.authenticateOlivier();
        final var before = new Date();

        // When
        client.get()
                .uri(ME)
                .header("Authorization", BEARER_PREFIX + olivier.jwt())
                .exchange()
                .expectStatus()
                .is2xxSuccessful();

        final var after = new Date();

        // Then
        final var user = userRepository.findByGithubUserId(olivier.user().getGithubUserId()).orElseThrow();
        assertThat(user.getLastSeenAt()).isAfter(before);
        assertThat(user.getLastSeenAt()).isBefore(after);
    }

    @Test
    void should_contains_first_and_last_names() {
        // Given
        final UserAuthHelper.AuthenticatedUser pierre = userAuthHelper.authenticatePierre();

        // When
        client.get()
                .uri(ME)
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
    void should_return_billing_profiles_and_missingPayoutPreference() {
        // Given
        final var authenticatedUser = userAuthHelper.signUpUser(
                faker.number().randomNumber(11, true), "another-fake-user",
                faker.internet().url(), false);

        // When user has no BP and no reward
        client.get()
                .uri(ME)
                .header("Authorization", "Bearer " + authenticatedUser.jwt())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                          "missingPayoutPreference": false
                        }
                        """);

        // When user has a not-verified BP and no reward
        final var individualBillingProfile = billingProfileService.createIndividualBillingProfile(UserId.of(authenticatedUser.user().getId()),
                faker.rickAndMorty().character(), null);
        client.get()
                .uri(ME)
                .header("Authorization", "Bearer " + authenticatedUser.jwt())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                          "missingPayoutPreference": false
                        }
                        """);

        // When user has a not-verified BP and some reward
        final var projectId = ProjectId.of("f39b827f-df73-498c-8853-99bc3f562723");
        final var sponsorId = sponsorHelper.create().id();
        final var programId = programHelper.create(sponsorId).id();
        final var usdc = currencyRepository.findByCode("USDC").orElseThrow().id();
        accountingService.createSponsorAccountWithInitialBalance(sponsorId,
                Currency.Id.of(usdc), null,
                new SponsorAccount.Transaction(ZonedDateTime.now(), SponsorAccount.Transaction.Type.DEPOSIT, Network.ETHEREUM, faker.random().hex(),
                        PositiveAmount.of(200000L),
                        faker.rickAndMorty().character(), faker.hacker().verb(), null));
        accountingService.allocate(sponsorId, programId, PositiveAmount.of(100000L), Currency.Id.of(usdc));
        accountingService.grant(programId, projectId, PositiveAmount.of(100000L), Currency.Id.of(usdc));
        sendRewardToRecipient(authenticatedUser.user().getGithubUserId(), 100L, projectId);
        client.get()
                .uri(ME)
                .header("Authorization", "Bearer " + authenticatedUser.jwt())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                          "missingPayoutPreference": true
                        }
                        """);

        // When the user set missing payout preferences
        updatePayoutPreferences(authenticatedUser.user().getGithubUserId(), individualBillingProfile.id(), projectId.value());
        client.get()
                .uri(ME)
                .header("Authorization", "Bearer " + authenticatedUser.jwt())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                          "missingPayoutPreference": false
                        }
                        """);

        // When the user gets his BP verification blocked
        accountingHelper.patchBillingProfile(individualBillingProfile.id().value(), null, VerificationStatus.CLOSED);
        client.get()
                .uri(ME)
                .header("Authorization", "Bearer " + authenticatedUser.jwt())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                          "missingPayoutPreference": false
                        }
                        """);

        // When the user gets his BP verified
        accountingHelper.patchBillingProfile(individualBillingProfile.id().value(), null, VerificationStatus.VERIFIED);
        client.get()
                .uri(ME)
                .header("Authorization", "Bearer " + authenticatedUser.jwt())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                          "missingPayoutPreference": false
                        }
                        """);

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
                .uri(ME)
                .header("Authorization", "Bearer " + authenticatedUser.jwt())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                          "missingPayoutPreference": false
                        }
                        """);
    }

    @Test
    void should_return_sponsors() {
        // Given
        final var authenticatedUser = userAuthHelper.signUpUser(
                faker.number().randomNumber(11, true), "another-fake-user",
                faker.internet().url(), false);

        // When user has no sponsor
        client.get()
                .uri(ME)
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

        addSponsorFor(authenticatedUser, UUID.fromString("58a0a05c-c81e-447c-910f-629817a987b8"));
        addSponsorFor(authenticatedUser, UUID.fromString("85435c9b-da7f-4670-bf65-02b84c5da7f0"));
        addSponsorFor(authenticatedUser, UUID.fromString("4202fd03-f316-458f-a642-421c7b3c7026"));

        // When user no sponsors
        client.get()
                .uri(ME)
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
                              "logoUrl": "https://www.ed92.org/wp-content/uploads/2021/06/captain-america-2-scaled.jpg"
                            },
                            {
                              "id": "85435c9b-da7f-4670-bf65-02b84c5da7f0",
                              "name": "AS Nancy Lorraine",
                              "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/951523516066154017.png"
                            },
                            {
                              "id": "4202fd03-f316-458f-a642-421c7b3c7026",
                              "name": "ChatGPT",
                              "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/4216570625498269873.png"
                            }
                          ]
                        }
                        """);
    }


    @Test
    void should_return_programs() {
        // Given
        final var authenticatedUser = userAuthHelper.create();

        // When user has no programs
        client.get()
                .uri(ME)
                .header("Authorization", "Bearer " + authenticatedUser.jwt())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.programs.length()").isEqualTo(0);


        final var sponsorId = SponsorId.of("eb04a5de-4802-4071-be7b-9007b563d48d");
        programHelper.create(sponsorId, authenticatedUser);
        programHelper.create(sponsorId, authenticatedUser);
        programHelper.create(sponsorId, authenticatedUser);

        // When user has programs
        client.get()
                .uri(ME)
                .header("Authorization", "Bearer " + authenticatedUser.jwt())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.programs.length()").isEqualTo(3);
    }


    private void sendRewardToRecipient(Long recipientId, Long amount, ProjectId projectId) {
        final UserAuthHelper.AuthenticatedUser pierre = userAuthHelper.authenticatePierre();
        final RewardRequest rewardRequest = new RewardRequest()
                .amount(BigDecimal.valueOf(amount))
                .currencyId(CurrencyHelper.USDC.value())
                .recipientId(recipientId)
                .items(List.of(
                        new RewardItemRequest().id("1217699807")
                                .type(RewardType.PULL_REQUEST)
                                .number(580L)
                                .repoId(498695724L)
                ));

        // When
        indexerApiWireMockServer.stubFor(WireMock.put(
                        WireMock.urlEqualTo("/api/v1/users/%s?forceRefresh=false".formatted(recipientId)))
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
